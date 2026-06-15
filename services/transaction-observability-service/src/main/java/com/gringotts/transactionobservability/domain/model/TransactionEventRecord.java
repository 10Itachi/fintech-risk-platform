package com.gringotts.transactionobservability.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "transaction_event_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_id", columnNames = "event_id"),
                @UniqueConstraint(name = "uk_transaction_id", columnNames = "transaction_id")
        },
        indexes = {
                @Index(
                        name = "idx_transaction_id",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_recorded_at",
                        columnList = "recorded_at"
                ),
                @Index(
                        name = "idx_occurred_at",
                        columnList = "occurred_at"
                ),
                @Index(
                        name = "idx_recorded_at_transaction_id",
                        columnList = "recorded_at, transaction_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @Column(name = "event_version", nullable = false, updatable = false)
    private int eventVersion;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "checksum", nullable = false, updatable = false, length = 64)
    private String checksum;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "source_service", nullable = false, updatable = false)
    private String sourceService;
}