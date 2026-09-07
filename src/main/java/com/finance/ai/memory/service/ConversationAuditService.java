package com.finance.ai.memory.service;

import com.finance.ai.chat.dto.ChatRequest;
import com.finance.ai.exception.ConversationNotFoundException;
import com.finance.ai.memory.model.*;
import com.finance.ai.memory.repository.*;
import com.finance.ai.model.MessageRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationAuditService {

    private final ConversationRepository conversationRepository;
    private final ConversationMessageAuditRepository messageRepository;

    /** Explicitly creates a new, empty conversation (used by POST /api/conversations). */
    @Transactional
    public Conversation createConversation(String userId) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        return conversationRepository.save(conversation);
    }

    /** Returns an existing conversation or creates a new one. */
    @Transactional
    public UUID resolveConversation(ChatRequest request) {
        String conversationId = request.getConversationId();
        String userId = request.getUserId();

        if (conversationId != null && !conversationId.isBlank()) {
            UUID uuId;
            try {
                uuId = UUID.fromString(conversationId);
            } catch (IllegalArgumentException e) {
                throw new ConversationNotFoundException("Invalid conversation id: " + conversationId);
            }
            if (conversationRepository.existsById(uuId)) {
                return uuId;
            }
            throw new ConversationNotFoundException("No conversation found with id " + conversationId);
        }

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setQuery(request.getMessage());
        return conversationRepository.save(conversation).getId();
    }

    @Transactional
    public void recordExchange(UUID conversationId, String query, String assistantReply) {
        save(conversationId, MessageRole.USER, query);
        save(conversationId, MessageRole.ASSISTANT, assistantReply);
    }

    private void save(UUID conversationId, MessageRole role, String content) {
        ConversationMessageAudit audit = new ConversationMessageAudit();
        audit.setConversationId(conversationId);
        audit.setRole(role);
        audit.setContent(content);
        messageRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public List<ConversationMessageAudit> getHistory(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional(readOnly = true)
    public boolean conversationExists(UUID conversationId) {
        return conversationRepository.existsById(conversationId);
    }
}