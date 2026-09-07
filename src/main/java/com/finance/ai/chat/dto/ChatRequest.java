package com.finance.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "User ID must not be empty")
    private String userId;

    @NotBlank(message = "Message must not be empty")
    @Size(max = 4000, message = "Message too long")
    private String message;

    private String conversationId;
}

