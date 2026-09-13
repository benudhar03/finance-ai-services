package com.finance.ai.rag.dto;

import com.finance.ai.rag.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DocumentSummary {

    private UUID id;
    private String fileName;
    private OffsetDateTime uploadedAt;
    private int chunkCount;
    private DocumentStatus status;
}