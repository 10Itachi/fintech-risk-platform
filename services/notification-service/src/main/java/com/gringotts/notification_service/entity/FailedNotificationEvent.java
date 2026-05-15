package com.gringotts.notification_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "failed_notification_events",

        indexes = {

                @Index(
                        name = "idx_failed_at",
                        columnList = "failed_at"
                ),

                @Index(
                        name = "idx_transaction_id",
                        columnList = "transaction_id"
                )
        }
)
@Getter
@Setter
public class FailedNotificationEvent {

    @Id
    @GeneratedValue
    @Column(
            name = "id",
            nullable = false,
            updatable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID id;

    @Column(
            name = "transaction_id",
            length = 100
    )
    private String transactionId;

    @Column(
            name = "source_topic",
            nullable = false,
            length = 150
    )
    private String sourceTopic;

    /*
     =========================================================
     LONG ERROR STACKTRACES
     =========================================================
     */
    @Lob
    @Column(
            name = "error_message",
            columnDefinition = "LONGTEXT"
    )
    private String errorMessage;

    /*
     =========================================================
     ORIGINAL FAILED PAYLOAD
     =========================================================
     */
    @Lob
    @Column(
            name = "payload",
            columnDefinition = "LONGTEXT"
    )
    private String payload;

    @Column(
            name = "failed_at",
            nullable = false
    )
    private Instant failedAt;
}