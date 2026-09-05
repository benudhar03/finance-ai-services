package com.finance.ai.chat.service;

import com.finance.ai.exception.LlmUnavailableException;
import com.finance.ai.memory.service.ConversationAuditService;
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
    private final ConversationAuditService auditService;

    public ChatResponse handleChat(ChatRequest request) {
        UUID conversationId = auditService.resolveConversation(request);
        try {
            String reply = llmService.generateReply(request.getMessage(), conversationId.toString());
            auditService.recordExchange(conversationId, request.getMessage(), reply);
            return new ChatResponse(reply, conversationId.toString());
        } catch (Exception e) {
            log.error("LLM call failed", e);
            throw new LlmUnavailableException("The assistant is temporarily unavailable. Please try again.");
        }
    }

}
