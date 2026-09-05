package com.finance.ai.llm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final ChatClient chatClient;

    public String generateReply(String prompt,String conversationId) {
        if (!StringUtils.hasText(prompt)) {
            return "";
        }
        String reply = chatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt.trim())
                .call()
                .content();

        log.debug("LLM call completed, response length={}", reply != null ? reply.length() : 0);
        return reply != null ? reply : "";
    }

}
