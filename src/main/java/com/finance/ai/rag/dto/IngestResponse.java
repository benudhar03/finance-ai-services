package com.finance.ai.rag.dto;

import com.finance.ai.rag.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class IngestResponse {
    private UUID documentId;
    private String fileName;
    private int chunksIndexed;
    private DocumentStatus status;
}