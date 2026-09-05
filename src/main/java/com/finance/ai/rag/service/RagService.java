package com.finance.ai.rag.service;

import org.springframework.stereotype.Service;
import com.finance.ai.rag.ingestion.DocumentIngestor;
import com.finance.ai.rag.retrieval.Retriever;
import com.finance.ai.rag.embedding.Embedder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagService {
    private final DocumentIngestor ingestor;
    private final Retriever retriever;
    private final Embedder embedder;
}
