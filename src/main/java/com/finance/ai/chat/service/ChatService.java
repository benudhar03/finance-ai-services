package com.finance.ai.chat.service;

import com.finance.ai.agent.IntentClassifierService;
import com.finance.ai.chat.dto.ChatRequest;
import com.finance.ai.chat.dto.ChatResponse;
import com.finance.ai.exception.LlmUnavailableException;
import com.finance.ai.exception.PromptGuardException;
import com.finance.ai.guardrail.OutputGuardService;
import com.finance.ai.guardrail.PromptGuardService;
import com.finance.ai.llm.service.ResilientLlmGateway;
import com.finance.ai.memory.service.ConversationAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ResilientLlmGateway llmGateway;
    private final ConversationAuditService auditService;
    private final IntentClassifierService intentClassifierService;
    private final PromptGuardService promptGuardService;
    private final OutputGuardService outputGuardService;

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

        String label = useRag ? "RAG" : "LLM";
        Supplier<CompletableFuture<String>> call = useRag
                ? () -> llmGateway.generateGroundedReplyAsync(request.getMessage(), conversationId.toString())
                : () -> llmGateway.generateReplyAsync(request.getMessage(), conversationId.toString());

        String reply = callGuarded(call, conversationId, label);

        var outputCheck = outputGuardService.screen(reply);
        if (outputCheck.flagged()) {
            log.warn("Flagged output for conversation {}: {}", conversationId, outputCheck.reason());
            reply = outputGuardService.sanitize(reply);
        }

        auditService.recordExchange(conversationId, request.getMessage(), reply);
        return new ChatResponse(reply, conversationId.toString(), classifiedAsRag);
    }

    private String callGuarded(Supplier<CompletableFuture<String>> call, UUID conversationId, String label) {
        try {
            return call.get().join();
        } catch (Exception e) {
            log.error("{} call failed for conversation {}", label, conversationId, e);
            throw new LlmUnavailableException("The assistant is temporarily unavailable. Please try again.");
        }
    }
}