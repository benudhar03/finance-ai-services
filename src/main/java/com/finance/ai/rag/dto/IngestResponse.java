package com.finance.ai.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngestResponse {
    private String fileName;
    private int chunksIndexed;
}