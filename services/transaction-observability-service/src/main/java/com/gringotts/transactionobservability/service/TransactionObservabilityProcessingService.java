package com.gringotts.transactionobservability.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transactionobservability.domain.model.ProcessedEvent;
import com.gringotts.transactionobservability.domain.model.TransactionEventRecord;
import com.gringotts.transactionobservability.repository.ProcessedEventRepository;
import com.gringotts.transactionobservability.repository.TransactionEventRecordRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TransactionObservabilityProcessingService {

    private final TransactionEventRecordRepository transactionEventRecordRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    /*
     =========================================================
     METRICS
     =========================================================
     */
    private final Counter processedCounter;
    private final Counter duplicateCounter;
    private final Counter skippedCounter;
    private final Counter failureCounter;

    private final Timer processingTimer;
    private final Timer dbWriteTimer;

    private final DistributionSummary payloadSizeMetric;

    public TransactionObservabilityProcessingService(
            TransactionEventRecordRepository transactionEventRecordRepository,
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.transactionEventRecordRepository =transactionEventRecordRepository;
        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;

        /*
         COUNTERS
         */
        this.processedCounter =
                meterRegistry.counter(
                        "transaction.observability.events.processed"
                );
        this.duplicateCounter =
                meterRegistry.counter(
                        "transaction.observability.events.duplicate"
                );
        this.skippedCounter =
                meterRegistry.counter(
                        "transaction.observability.events.skipped"
                );
        this.failureCounter =
                meterRegistry.counter(
                        "transaction.observability.events.failed"
                );

        /*
         TIMERS
         */
        this.processingTimer =
                meterRegistry.timer(
                        "transaction.observability.processing.time"
                );
        this.dbWriteTimer =
                meterRegistry.timer(
                        "transaction.observability.db.write.time"
                );

        /*
         PAYLOAD SIZE
         */
        this.payloadSizeMetric =
                meterRegistry.summary(
                        "transaction.observability.payload.bytes"
                );
    }
    @Transactional
    public void process(TransactionFinalizedEvent event) {

        long startTime = System.currentTimeMillis();

        UUID eventId = event.getEventId();
        UUID transactionId = event.getTransactionId();

        log.info(
                "event=kafka_event_received eventId={} txnId={} status={}",
                eventId,
                transactionId,
                event.getFinalStatus()
        );

        /*
         IDEMPOTENCY CHECK
         */
        if (processedEventRepository.existsByEventId(eventId)) {

            duplicateCounter.increment();

            log.warn(
                    "event=duplicate_event_detected eventId={} txnId={}",
                    eventId,
                    transactionId
            );
            return;
        }

        /*
         BUSINESS FILTER
         */
        if (!"APPROVED".equalsIgnoreCase(String.valueOf(event.getFinalStatus()))) {
            skippedCounter.increment();
            log.info(
                    "event=transaction_skipped txnId={} finalStatus={}",
                    transactionId,
                    event.getFinalStatus()
            );
            processedEventRepository.save(
                    ProcessedEvent.builder()
                            .eventId(eventId)
                            .processedAt(Instant.now())
                            .build()
            );
            return;
        }

        /*
         SERIALIZATION
         */
        String jsonPayload;
        try {
            jsonPayload =
                    objectMapper.writeValueAsString(event);
            payloadSizeMetric.record(
                    jsonPayload.getBytes().length
            );

        } catch (JsonProcessingException ex) {

            failureCounter.increment();

            log.error(
                    "event=event_serialization_failed eventId={} txnId={} message={}",
                    eventId,
                    transactionId,
                    ex.getMessage(),
                    ex
            );

            throw new RuntimeException(
                    "Failed to serialize event payload",
                    ex
            );
        }

        /*
         =========================================================
         DATABASE WRITE
         =========================================================
         */
        try {

            Timer.Sample dbTimer = Timer.start();

            String checksum =
                    ChecksumUtil.sha256(jsonPayload);

            TransactionEventRecord entry =
                    TransactionEventRecord.builder()
                            .eventId(eventId)
                            .transactionId(transactionId)
                            .eventType(event.getEventType())
                            .eventVersion(event.getEventVersion())
                            .payload(jsonPayload)
                            .checksum(checksum)
                            .occurredAt(event.getOccurredAt())
                            .recordedAt(Instant.now())
                            .sourceService("transaction-service")
                            .build();

            transactionEventRecordRepository.save(entry);

            dbTimer.stop(dbWriteTimer);

            log.info(
                    "event=transaction_record_persisted txnId={} eventId={}",
                    transactionId,
                    eventId
            );

        } catch (DataIntegrityViolationException ex) {

            duplicateCounter.increment();

            log.warn(
                    "event=duplicate_database_record txnId={} eventId={} message={}",
                    transactionId,
                    eventId,
                    ex.getMostSpecificCause().getMessage()
            );

            return;

        } catch (Exception ex) {

            failureCounter.increment();

            log.error(
                    "event=transaction_record_persistence_failed txnId={} eventId={} message={}",
                    transactionId,
                    eventId,
                    ex.getMessage(),
                    ex
            );

            throw ex;
        }

        /*
         =========================================================
         MARK EVENT AS PROCESSED
         =========================================================
         */
        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(eventId)
                        .processedAt(Instant.now())
                        .build()
        );

        processedCounter.increment();

        long duration =
                System.currentTimeMillis() - startTime;

        processingTimer.record(
                duration,
                TimeUnit.MILLISECONDS
        );

        log.info(
                "event=event_processing_completed eventId={} txnId={} durationMs={}",
                eventId,
                transactionId,
                duration
        );
    }
}