package com.finance.ai.rag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class PendingUploadStorage {

    private final Path baseDir;

    public PendingUploadStorage(@Value("${app.storage.pending-uploads-dir}") String baseDirPath) throws IOException {
        this.baseDir = Path.of(baseDirPath);
        Files.createDirectories(this.baseDir);
    }

    public String store(UUID documentId, MultipartFile file) throws IOException {
        Path target = baseDir.resolve(documentId + ".pdf");
        file.transferTo(target);
        return target.toAbsolutePath().toString();
    }

    public Path resolve(String storedFilePath) {
        return Path.of(storedFilePath);
    }

    public void delete(String storedFilePath) {
        try {
            Files.deleteIfExists(Path.of(storedFilePath));
        } catch (IOException ignored) {
            // best-effort cleanup; not fatal if it fails
        }
    }
}