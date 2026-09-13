package com.finance.ai.events;

import java.time.Instant;
import java.util.UUID;

public record DocumentUploadedEvent(
        UUID documentId,
        String storedFilePath,
        String originalFileName,
        Instant uploadedAt
) {}