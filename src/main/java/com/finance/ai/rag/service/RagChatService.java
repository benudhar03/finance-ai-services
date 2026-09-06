package com.finance.ai.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public String generateGroundedReply(String prompt, String conversationId) {
        if (!StringUtils.hasText(prompt)) {
            return "";
        }

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore).build();

        String reply = chatClient
                .prompt()
                .advisors(qaAdvisor)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt.trim())
                .call()
                .content();

        log.debug("RAG call completed, response length={}", reply != null ? reply.length() : 0);
        return reply != null ? reply : "";
    }
}