package com.finance.ai.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private UUID id;
    private String userId;
    private Instant createdAt;
}