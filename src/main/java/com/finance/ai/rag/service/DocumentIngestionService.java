package com.finance.ai.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter =
            TokenTextSplitter.builder().build();

    public int ingestPdf(Resource pdfResource, String sourceFileName) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                pdfResource,
                PdfDocumentReaderConfig.defaultConfig()
        );

        List<Document> pages = reader.get();
        pages.forEach(doc -> doc.getMetadata().put("source_file", sourceFileName));

        List<Document> chunks = textSplitter.apply(pages);

        vectorStore.add(chunks);
        log.info("Ingested {} chunks from {}", chunks.size(), sourceFileName);
        return chunks.size();
    }
}