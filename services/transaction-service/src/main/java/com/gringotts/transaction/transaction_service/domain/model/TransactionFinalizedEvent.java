package com.gringotts.transaction.transaction_service.domain.model;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionFinalizedEvent {

    private UUID transactionId;
    private Long userId;
    private BigDecimal amount;
    private Channel channel;
    private String country;

    private TransactionStatus finalStatus;

    // risk snapshot
    private Integer riskScore;
    private Double fraudProbability;
    private List<String> reasonCodes;

    // governance
    private String policyVersion;
    private String modelVersion;

    private Instant occurredAt;
}

