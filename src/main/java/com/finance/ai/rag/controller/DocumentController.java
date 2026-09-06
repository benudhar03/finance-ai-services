package com.finance.ai.rag.controller;

import com.finance.ai.rag.dto.IngestResponse;
import com.finance.ai.rag.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<IngestResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Resource resource = new InputStreamResource(file.getInputStream());
        int chunkCount = ingestionService.ingestPdf(resource, file.getOriginalFilename());
        return ResponseEntity.ok(new IngestResponse(file.getOriginalFilename(), chunkCount));
    }
}