package com.finance.ai.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String reply;
    private String conversationId;
    private Boolean classifiedAsRag;
}

