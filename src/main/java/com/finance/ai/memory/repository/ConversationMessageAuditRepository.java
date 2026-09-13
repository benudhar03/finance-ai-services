package com.finance.ai.memory.repository;

import java.util.List;
import java.util.UUID;
import com.finance.ai.memory.model.ConversationMessageAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface ConversationMessageAuditRepository extends JpaRepository<ConversationMessageAudit, UUID> {

    List<ConversationMessageAudit> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
    List<ConversationMessageAudit> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
}