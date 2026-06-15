package com.gringotts.notification_service.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.notification_service.entity.NotificationProcessed;
import com.gringotts.notification_service.repository.NotificationProcessedRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class NotificationProcessingService {

    private final NotificationProcessedRepository processedRepo;
    private final NotificationSender sender;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private Counter sentCounter;
    private Counter duplicateCounter;
    private Counter skippedCounter;
    private Counter failureCounter;

    public NotificationProcessingService(NotificationProcessedRepository processedRepo, NotificationSender sender, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.processedRepo = processedRepo;
        this.sender = sender;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;

        // Initialize metrics exactly ONCE when the application starts
        this.duplicateCounter = meterRegistry.counter("notification.events.duplicates");
        this.skippedCounter = meterRegistry.counter("notification.events.skipped");
        this.failureCounter = meterRegistry.counter("notification.events.failures");
        this.sentCounter = meterRegistry.counter("notification.event.sent");
    }

    @Transactional
    public void process(TransactionFinalizedEvent event) {

        UUID eventId = event.getEventId();
        UUID txnId = event.getTransactionId();

        log.info("event=NOTIFICATION_RECEIVED eventId={} txnId={} status={}",
                eventId, txnId, event.getFinalStatus());

        // 1) Idempotency
        if (processedRepo.existsByEventId(eventId)) {
            duplicateCounter.increment();
            log.warn("event=NOTIFICATION_DUPLICATE eventId={} txnId={}", eventId, txnId);
            return;
        }

        // 2) Business filter
        if (!"APPROVED".equalsIgnoreCase(String.valueOf(event.getFinalStatus()))) {
            skippedCounter.increment();

            processedRepo.save(NotificationProcessed.builder()
                    .eventId(eventId)
                    .processedAt(Instant.now())
                    .build());

            log.info("event=NOTIFICATION_SKIPPED txnId={} status={}", txnId, event.getFinalStatus());
            return;
        }

        // 3) Send notification
        try {
            sender.send(event);
            sentCounter.increment();

        } catch (Exception ex) {
            failureCounter.increment();
            log.error("event=NOTIFICATION_SEND_FAILED eventId={} txnId={}",
                    eventId, txnId, ex);
            throw ex; // → retry
        }

        // 4) Mark processed (last)
        try {
            processedRepo.save(NotificationProcessed.builder()
                    .eventId(eventId)
                    .processedAt(Instant.now())
                    .build());
        } catch (DataIntegrityViolationException dup) {
            // race condition safe
            duplicateCounter.increment();
            log.warn("event=NOTIFICATION_DUP_DB eventId={} txnId={}", eventId, txnId);
        }

        log.info("event=NOTIFICATION_PROCESSED eventId={} txnId={}", eventId, txnId);
    }
}