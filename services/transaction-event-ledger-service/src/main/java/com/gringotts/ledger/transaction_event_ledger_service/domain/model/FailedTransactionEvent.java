package com.gringotts.ledger.transaction_event_ledger_service.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "failed_transaction_events")
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
