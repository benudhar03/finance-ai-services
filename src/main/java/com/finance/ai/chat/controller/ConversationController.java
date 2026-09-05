package com.finance.ai.chat.controller;

import com.finance.ai.chat.dto.ConversationResponse;
import com.finance.ai.chat.dto.CreateConversationRequest;
import com.finance.ai.chat.dto.MessageResponse;
import com.finance.ai.exception.ConversationNotFoundException;
import com.finance.ai.memory.model.Conversation;
import com.finance.ai.memory.model.ConversationMessageAudit;
import com.finance.ai.memory.service.ConversationAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationAuditService auditService;

    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @RequestBody(required = false) CreateConversationRequest request) {
        String userId = request != null ? request.getUserId() : null;
        Conversation conversation = auditService.createConversation(userId);
        return ResponseEntity.ok(new ConversationResponse(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getCreatedAt()
        ));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable UUID id) {
        if (!auditService.conversationExists(id)) {
            throw new ConversationNotFoundException("No conversation found with id " + id);
        }
        List<ConversationMessageAudit> history = auditService.getHistory(id);
        List<MessageResponse> response = history.stream()
                .map(m -> new MessageResponse(m.getRole(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}