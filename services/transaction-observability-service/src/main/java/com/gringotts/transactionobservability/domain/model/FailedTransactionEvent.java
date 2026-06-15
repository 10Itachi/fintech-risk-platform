package com.gringotts.transactionobservability.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "failed_transaction_events",

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
@Setter
@Getter
public class FailedTransactionEvent {

    @Id
    @GeneratedValue
    private UUID id;

    private String transactionId;
    private String sourceTopic;

    @Lob // Use LOB to handle long stack traces
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    @Lob
    private String payload;
    private Instant failedAt;


}
