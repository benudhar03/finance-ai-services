package com.finance.ai.chat.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.finance.ai.chat.dto.ChatRequest;
import com.finance.ai.chat.dto.ChatResponse;
import com.finance.ai.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.handleChat(request));
    }

    @PostMapping("/rag")
    public ResponseEntity<ChatResponse> chatRag(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.handleRagChat(request));
    }

    @PostMapping("/agent")
    public ResponseEntity<ChatResponse> chatAgent(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.handleAgentChat(request));
    }
}
