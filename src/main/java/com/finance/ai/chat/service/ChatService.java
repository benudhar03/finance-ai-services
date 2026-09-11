package com.finance.ai.chat.service;

import com.finance.ai.agent.IntentClassifierService;
import com.finance.ai.exception.LlmUnavailableException;
import com.finance.ai.exception.PromptGuardException;
import com.finance.ai.guardrail.OutputGuardService;
import com.finance.ai.guardrail.PromptGuardService;
import com.finance.ai.memory.service.ConversationAuditService;
import com.finance.ai.rag.service.RagChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.finance.ai.chat.dto.ChatRequest;
import com.finance.ai.chat.dto.ChatResponse;
import com.finance.ai.llm.service.LlmService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

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
        try {
            String reply = useRag
                    ? ragChatService.generateGroundedReply(request.getMessage(), conversationId.toString())
                    : llmService.generateReply(request.getMessage(), conversationId.toString());

            var outputCheck = outputGuardService.screen(reply);
            if (outputCheck.flagged()) {
                log.warn("Flagged output for conversation {}: {}", conversationId, outputCheck.reason());
                reply = outputGuardService.sanitize(reply);
            }
            auditService.recordExchange(conversationId, request.getMessage(), reply);
            return new ChatResponse(reply, conversationId.toString(), classifiedAsRag);
        } catch (Exception e) {
            log.error("LLM call failed", e);
            throw new LlmUnavailableException("The assistant is temporarily unavailable. Please try again.");
        }
    }
}