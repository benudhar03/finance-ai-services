package com.finance.ai.rag.controller;

import com.finance.ai.events.DocumentUploadedEvent;
import com.finance.ai.exception.DocumentNotFoundException;
import com.finance.ai.exception.EventPublishException;
import com.finance.ai.rag.dto.DocumentSummary;
import com.finance.ai.rag.dto.IngestResponse;
import com.finance.ai.rag.model.DocumentStatus;
import com.finance.ai.rag.model.UploadedDocument;
import com.finance.ai.rag.repository.DocumentRepository;
import com.finance.ai.rag.service.DocumentIngestionEventProducer;
import com.finance.ai.rag.service.DocumentIngestionService;
import com.finance.ai.rag.service.PdfFileValidator;
import com.finance.ai.rag.service.PendingUploadStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final PdfFileValidator pdfFileValidator;
    private final DocumentRepository documentRepository;
    private final PendingUploadStorage pendingUploadStorage;
    private final DocumentIngestionService ingestionService;
    private final DocumentIngestionEventProducer eventProducer;


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<IngestResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        pdfFileValidator.validate(file);

        UUID documentId = UUID.randomUUID();
        String storedPath = pendingUploadStorage.store(documentId, file);

        UploadedDocument pending = new UploadedDocument();
        pending.setId(documentId);
        pending.setFileName(file.getOriginalFilename());
        pending.setChunkCount(0);
        pending.setStatus(DocumentStatus.PENDING);
        documentRepository.save(pending);

        try {
            eventProducer.publish(new DocumentUploadedEvent(
                    documentId, storedPath, file.getOriginalFilename(), Instant.now()));
        } catch (EventPublishException e) {
            pending.setStatus(DocumentStatus.FAILED);
            pending.setErrorMessage("Failed to queue for processing: " + e.getMessage());
            documentRepository.save(pending);
            pendingUploadStorage.delete(storedPath);
            throw e; // GlobalExceptionHandler maps this to a 502
        }
        return ResponseEntity.accepted()
                .body(new IngestResponse(documentId, file.getOriginalFilename(), 0, DocumentStatus.PENDING));
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummary>> list() {
        List<DocumentSummary> summaries = ingestionService.listDocuments().stream()
                .map(d -> new DocumentSummary(d.getId(), d.getFileName(), d.getUploadedAt(), d.getChunkCount(), d.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<IngestResponse> getStatus(@PathVariable UUID id) {
        UploadedDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("No document found with id " + id));
        return ResponseEntity.ok(new IngestResponse(doc.getId(), doc.getFileName(), doc.getChunkCount(), doc.getStatus()));
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