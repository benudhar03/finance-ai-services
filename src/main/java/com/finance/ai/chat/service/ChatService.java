package com.finance.ai.chat.service;

import com.finance.ai.agent.IntentClassifierService;
import com.finance.ai.exception.LlmUnavailableException;
import com.finance.ai.exception.PromptGuardException;
import com.finance.ai.guardrail.OutputGuardService;
import com.finance.ai.guardrail.PromptGuardService;
import com.finance.ai.memory.service.ConversationAuditService;
import com.finance.ai.rag.service.RagChatService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.finance.ai.chat.dto.ChatRequest;
import com.finance.ai.chat.dto.ChatResponse;
import com.finance.ai.llm.service.LlmService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final LlmService llmService;
    private final RagChatService ragChatService;
    private final ConversationAuditService auditService;
    private final PromptGuardService promptGuardService;
    private final OutputGuardService outputGuardService;
    private final IntentClassifierService intentClassifierService;

    @Qualifier("llmExecutor")
    private final ExecutorService llmExecutor;

    public ChatResponse handleChat(ChatRequest request) {
        return process(request, false);
    }

    public ChatResponse handleRagChat(ChatRequest request) {
        return process(request, true);
    }

    public ChatResponse handleAgentChat(ChatRequest request) {
        UUID conversationId = auditService.resolveConversation(request);
        boolean useRag = intentClassifierService.requiresDocumentRetrieval(request.getMessage());
        return processWithResolvedId(request, useRag, useRag, conversationId);
    }

    private ChatResponse process(ChatRequest request, boolean useRag) {
        UUID conversationId = auditService.resolveConversation(request);
        return processWithResolvedId(request, useRag, null, conversationId);
    }

    private ChatResponse processWithResolvedId(ChatRequest request, boolean useRag, Boolean classifiedAsRag, UUID conversationId) {
        var inputCheck = promptGuardService.screenUserInput(request.getMessage());
        if (inputCheck.flagged()) {
            log.warn("Flagged input for conversation {}: {}", conversationId, inputCheck.reason());
            throw new PromptGuardException("Your message could not be processed. Please rephrase.");
        }
        String reply = useRag
                ? callRagChatGuarded(request.getMessage(), conversationId.toString())
                : callLlmGuarded(request.getMessage(), conversationId.toString());

        var outputCheck = outputGuardService.screen(reply);
        if (outputCheck.flagged()) {
            log.warn("Flagged output for conversation {}: {}", conversationId, outputCheck.reason());
            reply = outputGuardService.sanitize(reply);
        }
        auditService.recordExchange(conversationId, request.getMessage(), reply);
        return new ChatResponse(reply, conversationId.toString(), classifiedAsRag);
    }

    private String callRagChatGuarded(String message, String conversationId) {
        try {
            return callRagAsync(message, conversationId).join();
        } catch (Exception e) {
            log.error("RAG call failed for conversation {}", conversationId, e);
            throw new LlmUnavailableException("The assistant is temporarily unavailable. Please try again.");
        }
    }

    private String callLlmGuarded(String message, String conversationId) {
        try {
            return callLlmAsync(message, conversationId).join();
        } catch (Exception e) {
            log.error("LLM call failed for conversation {}", conversationId, e);
            throw new LlmUnavailableException("The assistant is temporarily unavailable. Please try again.");
        }
    }

    @CircuitBreaker(name = "llmService", fallbackMethod = "llmFallback")
    @TimeLimiter(name = "llmService")
    public CompletableFuture<String> callLlmAsync(String message, String conversationId) {
        return CompletableFuture.supplyAsync(
                () -> llmService.generateReply(message, conversationId), llmExecutor);
    }

    @CircuitBreaker(name = "ragChatService", fallbackMethod = "ragFallback")
    @TimeLimiter(name = "ragChatService")
    public CompletableFuture<String> callRagAsync(String message, String conversationId) {
        return CompletableFuture.supplyAsync(
                () -> ragChatService.generateGroundedReply(message, conversationId), llmExecutor);
    }

    private CompletableFuture<String> llmFallback(String message, String conversationId, Throwable t) {
        log.warn("llmService circuit breaker fallback triggered for conversation {}: {}", conversationId, t.toString());
        return CompletableFuture.failedFuture(t);
    }

    private CompletableFuture<String> ragFallback(String message, String conversationId, Throwable t) {
        log.warn("ragChatService circuit breaker fallback triggered for conversation {}: {}", conversationId, t.toString());
        return CompletableFuture.failedFuture(t);
    }
}