package com.finance.ai.chat.service;

import com.finance.ai.agent.IntentClassifierService;
import com.finance.ai.agent.model.QueryIntent;
import com.finance.ai.agent.model.QueryIntentResult;
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

    private static final int HISTORY_MESSAGE_LIMIT = 5;

    private final ResilientLlmGateway llmGateway;
    private final ConversationAuditService auditService;
    private final IntentClassifierService intentClassifierService;
    private final PromptGuardService promptGuardService;
    private final OutputGuardService outputGuardService;

    public ChatResponse handleChat(ChatRequest request) {
        UUID conversationId = auditService.resolveConversation(request);
        return process(request, conversationId, false);
    }

    public ChatResponse handleRagChat(ChatRequest request) {
        UUID conversationId = auditService.resolveConversation(request);
        return process(request, conversationId, true);
    }

    public ChatResponse handleAgentChat(ChatRequest request) {
        UUID conversationId = auditService.resolveConversation(request);
        return process(request, conversationId, false);
    }

    private ChatResponse process(ChatRequest request, UUID conversationId, boolean forceRag) {
        var inputCheck = promptGuardService.screenUserInput(request.getMessage());
        if (inputCheck.flagged()) {
            log.warn("Flagged input for conversation {}: {}", conversationId, inputCheck.reason());
            throw new PromptGuardException("Your message could not be processed. Please rephrase.");
        }

        String history = auditService.getRecentHistory(conversationId, HISTORY_MESSAGE_LIMIT);
        QueryIntentResult intentResult = intentClassifierService.classify(request.getMessage(), history);

        // Short-circuit before any expensive/risky LLM generation call
        if (intentResult.intent() == QueryIntent.OUT_OF_SCOPE) {
            return shortCircuit(request, conversationId,
                    "I'm focused on personal finance topics — I'm not able to help with that here.");
        }
        if (intentResult.intent() == QueryIntent.ADVICE_REQUEST) {
            return shortCircuit(request, conversationId,
                    "I can explain the concepts involved, but personalized investment, tax, or legal advice needs a licensed professional — I'd recommend speaking with one for your specific situation.");
        }

        boolean useRag = forceRag || intentResult.requiresDocumentRetrieval();
        String queryForModel = intentResult.enrichedQuery();

        Supplier<CompletableFuture<String>> call = useRag
                ? () -> llmGateway.generateGroundedReplyAsync(queryForModel, conversationId.toString())
                : () -> llmGateway.generateReplyAsync(queryForModel, conversationId.toString());

        String reply = callGuarded(call, conversationId, useRag ? "RAG" : "LLM");

        var outputCheck = outputGuardService.screen(reply);
        if (outputCheck.flagged()) {
            log.warn("Flagged output for conversation {}: {}", conversationId, outputCheck.reason());
            reply = outputGuardService.sanitize(reply);
        }

        auditService.recordExchange(conversationId, request.getMessage(), reply);
        return new ChatResponse(reply, conversationId.toString(), useRag);
    }

    private ChatResponse shortCircuit(ChatRequest request, UUID conversationId, String reply) {
        auditService.recordExchange(conversationId, request.getMessage(), reply);
        return new ChatResponse(reply, conversationId.toString(), false);
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