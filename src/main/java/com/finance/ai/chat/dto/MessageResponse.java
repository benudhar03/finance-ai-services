package com.finance.ai.chat.dto;

import com.finance.ai.model.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private MessageRole role;
    private String content;
    private Instant createdAt;
}