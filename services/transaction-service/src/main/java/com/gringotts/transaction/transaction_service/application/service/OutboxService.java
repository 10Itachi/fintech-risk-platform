package com.gringotts.transaction.transaction_service.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gringotts.enums.OutboxStatus;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transaction.transaction_service.domain.exception.OutboxSerializationException;
import com.gringotts.transaction.transaction_service.domain.model.OutboxEvent;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {

    private static final String AGGREGATE_TYPE =
            "TRANSACTION";

    private static final String EVENT_TYPE =
            "TRANSACTION_FINALIZED";

    private final ObjectMapper objectMapper;


    public OutboxService(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    /*
     =========================================================
     BUILD TRANSACTION FINALIZED OUTBOX EVENT
     =========================================================
     */
    public OutboxEvent buildTransactionFinalizedEvent(
            Transaction txn
    ) {

        String correlationId =
                MDC.get("X-Correlation-ID");

        UUID eventId = UUID.randomUUID();

        Instant now = Instant.now();

        TransactionFinalizedEvent event =
                TransactionFinalizedEvent.builder()
                        .eventId(eventId)
                        .eventType(EVENT_TYPE)
                        .eventVersion(1)
                        .occurredAt(now)
                        .transactionId(txn.getTransactionId())
                        .userId(String.valueOf(txn.getUserId()))
                        .userName(txn.getUserName())
                        .email(txn.getEmail())
                        .amount(txn.getAmount())
                        .finalStatus(txn.getTransactionStatus())
                        .correlationId(correlationId)
                        .build();

        try {

            String payload =
                    objectMapper.writeValueAsString(event);

            return OutboxEvent.builder()
                    .id(eventId)
                    .aggregateId(txn.getTransactionId())
                    .aggregateType(AGGREGATE_TYPE)
                    .eventType(EVENT_TYPE)
                    .eventVersion(1)
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .nextRetryAt(now)
                    .createdAt(now)
                    .correlationId(event.getCorrelationId())
                    .build();

        } catch (JsonProcessingException ex) {

            throw new OutboxSerializationException(
                    "Outbox Failed to serialize transaction finalized event"
            );
        }
    }
}
