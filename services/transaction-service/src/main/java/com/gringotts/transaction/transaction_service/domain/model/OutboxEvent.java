package com.gringotts.transaction.transaction_service.domain.model;

import com.gringotts.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox_event",
        indexes = {
                @Index(
                        name = "idx_status_next_retry",
                        columnList = "status, next_retry_at"
                ),
                @Index(
                        name = "idx_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_aggregate_id",
                        columnList = "aggregate_id"
                )
        }
)
public class OutboxEvent {

    @Id
    @Column(
            nullable = false,
            updatable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID id;

    /*
     =====================================================
     BUSINESS ENTITY THAT GENERATED EVENT
     =====================================================
     */
    @Column(
            name = "aggregate_id",
            nullable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID aggregateId;

    /*
     =====================================================
     AGGREGATE TYPE
     Example:
     TRANSACTION
     PAYMENT
     USER
     =====================================================
     */
    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 100
    )
    private String aggregateType;

    /*
     =====================================================
     EVENT TYPE
     =====================================================
     */
    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false
    )
    private int eventVersion;

    /*
     =====================================================
     SERIALIZED DOMAIN EVENT
     =====================================================
     */
    @Lob
    @Column(
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    private Instant nextRetryAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant sentAt;

    private String correlationId;
}
