package com.finance.ai.agent.model;

public record QueryIntentResult(
        QueryIntent intent,
        String enrichedQuery,
        boolean requiresDocumentRetrieval,
        boolean requiresAdvisorDisclaimer
) {}