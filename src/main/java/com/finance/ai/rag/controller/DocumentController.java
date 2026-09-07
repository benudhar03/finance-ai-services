package com.finance.ai.rag.controller;

import com.finance.ai.exception.DocumentNotFoundException;
import com.finance.ai.rag.dto.DocumentSummary;
import com.finance.ai.rag.dto.IngestResponse;
import com.finance.ai.rag.model.UploadedDocument;
import com.finance.ai.rag.repository.DocumentRepository;
import com.finance.ai.rag.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final DocumentRepository documentRepository;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<IngestResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Resource resource = new InputStreamResource(file.getInputStream());
        UUID documentId = ingestionService.ingestPdf(resource, file.getOriginalFilename());
        int chunkCount = documentRepository.findById(documentId)
                .map(UploadedDocument::getChunkCount)
                .orElse(0);
        return ResponseEntity.ok(new IngestResponse(documentId, file.getOriginalFilename(), chunkCount));
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummary>> list() {
        List<DocumentSummary> summaries = ingestionService.listDocuments().stream()
                .map(d -> new DocumentSummary(d.getId(), d.getFileName(), d.getUploadedAt(), d.getChunkCount()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException("No document found with id " + id);
        }
        ingestionService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}