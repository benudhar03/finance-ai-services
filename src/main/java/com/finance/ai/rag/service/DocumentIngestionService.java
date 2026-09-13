package com.finance.ai.rag.service;

import com.finance.ai.rag.model.DocumentStatus;
import com.finance.ai.rag.model.UploadedDocument;
import org.springframework.ai.document.Document;
import com.finance.ai.rag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final TokenTextSplitter textSplitter = TokenTextSplitter.builder().build();

    @Transactional
    public void processIngestion(UUID documentId, Resource pdfResource, String sourceFileName) {
        UploadedDocument record = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document record missing for id " + documentId));

        // idempotency guard — Kafka is at-least-once, so a redelivered message shouldn't reprocess a finished document
        if (record.getStatus() == DocumentStatus.READY) {
            log.info("Document {} already READY, skipping duplicate ingestion event", documentId);
            return;
        }
        record.setStatus(DocumentStatus.PROCESSING);
        documentRepository.save(record);
        try {
            PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, PdfDocumentReaderConfig.defaultConfig());
            List<Document> pages = reader.get();
            pages.forEach(doc -> {
                doc.getMetadata().put("source_file", sourceFileName);
                doc.getMetadata().put("document_id", documentId.toString());
            });

            List<Document> chunks = textSplitter.apply(pages);
            vectorStore.add(chunks);

            record.setChunkCount(chunks.size());
            record.setStatus(DocumentStatus.READY);
            documentRepository.save(record);

            log.info("Ingested {} chunks from {} (documentId={})", chunks.size(), sourceFileName, documentId);
        } catch (Exception e) {
            record.setStatus(DocumentStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            documentRepository.save(record);
            log.error("Ingestion failed for documentId={}", documentId, e);
            throw e; // rethrow so Kafka's error handler can apply retry/DLT logic
        }
    }

    //Can be used for the direct Call
    @Transactional
    public UUID ingestPdf(Resource pdfResource, String sourceFileName) {
        UUID documentId = UUID.randomUUID();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                pdfResource,
                PdfDocumentReaderConfig.defaultConfig()
        );

        List<Document> pages = reader.get();
        pages.forEach(doc -> {
            doc.getMetadata().put("source_file", sourceFileName);
            doc.getMetadata().put("document_id", documentId.toString());
        });

        List<Document> chunks = textSplitter.apply(pages);
        vectorStore.add(chunks);

        UploadedDocument documentRecord = new UploadedDocument();
        documentRecord.setId(documentId);
        documentRecord.setFileName(sourceFileName);
        documentRecord.setChunkCount(chunks.size());
        documentRepository.save(documentRecord);

        log.info("Ingested {} chunks from {} (documentId={})", chunks.size(), sourceFileName, documentId);
        return documentId;
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        vectorStore.delete("document_id == '" + documentId + "'");
        documentRepository.deleteById(documentId);
        log.info("Deleted document {} and its vector chunks", documentId);
    }

    public List<UploadedDocument> listDocuments() {
        return documentRepository.findAllByOrderByUploadedAtDesc();
    }
}