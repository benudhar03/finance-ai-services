package com.finance.ai.memory.repository;

import com.finance.ai.memory.model.ConversationMessageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConversationMessageAuditRepository
        extends JpaRepository<ConversationMessageAudit, UUID> {

    List<ConversationMessageAudit> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}