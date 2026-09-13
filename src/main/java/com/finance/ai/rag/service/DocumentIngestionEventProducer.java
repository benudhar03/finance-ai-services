package com.finance.ai.rag.service;

import com.finance.ai.events.DocumentUploadedEvent;
import com.finance.ai.exception.EventPublishException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class DocumentIngestionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;
    private final long publishTimeoutMs;

    public DocumentIngestionEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.ingestion.topic}") String topic,
            @Value("${app.ingestion.publish-timeout-ms:10000}") long publishTimeoutMs) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.publishTimeoutMs = publishTimeoutMs;
    }

    /**
     * Publishes and blocks for broker acknowledgement, bounded by publishTimeoutMs.
     * Throws EventPublishException on any failure — callers MUST handle this rather
     * than ignore it, since a silently-lost event means a document stuck in PENDING
     * forever with no path to ever becoming READY or FAILED.
     */
    public void publish(DocumentUploadedEvent event) {
        validate(event);
        String key = event.documentId().toString();
        try {
            SendResult<String, Object> result =
                    kafkaTemplate.send(topic, key, event).get(publishTimeoutMs, TimeUnit.MILLISECONDS);

            logPublishSuccess(result, key);
        } catch (TimeoutException e) {
            log.error("Timed out publishing DocumentUploadedEvent documentId={} after {}ms",
                    event.documentId(), publishTimeoutMs, e);
            throw new EventPublishException(
                    "Timed out publishing ingestion event for document " + event.documentId(), e);

        } catch (ExecutionException e) {
            log.error("Broker rejected DocumentUploadedEvent documentId={}", event.documentId(), e.getCause());
            throw new EventPublishException(
                    "Failed to publish ingestion event for document " + event.documentId(), e.getCause());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing DocumentUploadedEvent documentId={}", event.documentId(), e);
            throw new EventPublishException(
                    "Interrupted while publishing ingestion event for document " + event.documentId(), e);
        }
    }

    private void logPublishSuccess(SendResult<String, Object> result, String documentId) {
        var metadata = result.getRecordMetadata();
        log.info(
                "Published DocumentUploadedEvent documentId={} topic={} partition={} offset={}",
                documentId,
                metadata.topic(),
                metadata.partition(),
                metadata.offset()
        );
    }

    private void validate(DocumentUploadedEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(event.documentId(), "documentId must not be null");
        if (!StringUtils.hasText(event.storedFilePath())) {
            throw new IllegalArgumentException("storedFilePath must not be blank for documentId=" + event.documentId());
        }
        if (!StringUtils.hasText(event.originalFileName())) {
            throw new IllegalArgumentException("originalFileName must not be blank for documentId=" + event.documentId());
        }
    }
}