package com.finance.ai.rag.repository;

import com.finance.ai.rag.model.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<UploadedDocument, UUID> {
    List<UploadedDocument> findAllByOrderByUploadedAtDesc();
}