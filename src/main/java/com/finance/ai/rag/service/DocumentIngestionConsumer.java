package com.finance.ai.rag.service;

import com.finance.ai.events.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.io.FileSystemResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentIngestionConsumer {

    private final DocumentIngestionService ingestionService;
    private final PendingUploadStorage pendingUploadStorage;

    @KafkaListener(topics = "${app.ingestion.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onDocumentUploaded(ConsumerRecord<String, DocumentUploadedEvent> record, Acknowledgment ack) {
        DocumentUploadedEvent event = record.value();
        log.info("Consuming DocumentUploadedEvent documentId={} partition={} offset={}",
                event != null ? event.documentId() : "null", record.partition(), record.offset());

        validate(event);

        var resource = new FileSystemResource(pendingUploadStorage.resolve(event.storedFilePath()));
        ingestionService.processIngestion(event.documentId(), resource, event.originalFileName());
        pendingUploadStorage.delete(event.storedFilePath());

        // Offset only commits once processing (including the DB status write) has
        // durably succeeded. If the consumer crashes before this line, the broker
        // redelivers the message on restart — safe, because processIngestion's
        // READY-status guard makes reprocessing a no-op if it already finished.
        ack.acknowledge();
        log.info("Committed offset documentId={} partition={} offset={}",
                event.documentId(), record.partition(), record.offset());
    }

    private void validate(DocumentUploadedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Received null DocumentUploadedEvent");
        }
        if (event.documentId() == null) {
            throw new IllegalArgumentException("DocumentUploadedEvent missing documentId");
        }
        if (!StringUtils.hasText(event.storedFilePath())) {
            throw new IllegalArgumentException("DocumentUploadedEvent missing storedFilePath for documentId=" + event.documentId());
        }
    }
}