package com.finance.ai.chat.dto;

import lombok.Data;

@Data
public class CreateConversationRequest {
    private String userId; // optional — null for anonymous/guest conversations
}