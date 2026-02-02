package com.gringotts.ledger.transaction_event_ledger_service.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transaction_event_ledger",
        uniqueConstraints = @UniqueConstraint(columnNames = "event_id")
)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier of THIS event (idempotency at event level)
     */
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    /**
     * Business reference (for search & reconciliation)
     */
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    /**
     * Type of event (e.g. TRANSACTION_FINALIZED)
     */
    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    /**
     * Schema version of the event
     */
    @Column(name = "event_version", nullable = false, updatable = false)
    private String eventVersion;

    /**
     * 🔐 RAW IMMUTABLE EVENT
     * EXACT JSON received from Kafka
     */
    @Lob
    @Column(
            name = "event_payload",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String eventPayload;

    /**
     * When the event occurred in source system
     */
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    /**
     * When THIS ledger recorded the event
     */
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    /**
     * Who produced this event
     */
    @Column(name = "source_service", nullable = false, updatable = false)
    private String sourceService;

    /**
     * Integrity proof (e.g. SHA-256 of payload)
     */
    @Column(name = "checksum", nullable = false, updatable = false)
    private String checksum;
}

