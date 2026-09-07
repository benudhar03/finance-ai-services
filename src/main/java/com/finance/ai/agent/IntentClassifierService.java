package com.finance.ai.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IntentClassifierService {

    private static final String CLASSIFIER_SYSTEM_PROMPT = """
            You are a routing classifier for a finance assistant. Decide whether the
            user's question requires looking up specific information from previously
            uploaded documents (receipts, filings, fact sheets, statements).

            Respond with exactly one word: YES or NO.
            - YES if the question asks about specific figures, terms, transaction details,
              or content that would only be found in an uploaded document.
            - NO if the question is general financial knowledge, a calculation request,
              or casual conversation.
            """;

    private final ChatClient classifierChatClient;

    public IntentClassifierService(@Qualifier("classifierChatClient") ChatClient classifierChatClient) {
        this.classifierChatClient = classifierChatClient;
    }

    public boolean requiresDocumentRetrieval(String userMessage) {
        String decision = classifierChatClient
                .prompt()
                .system(CLASSIFIER_SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();

        boolean needsRag = decision != null && decision.trim().equalsIgnoreCase("YES");
        log.info("INTENT CLASSIFIED: message=\"{}\" -> requiresDocumentRetrieval={}", userMessage, needsRag);
        return needsRag;
    }
}