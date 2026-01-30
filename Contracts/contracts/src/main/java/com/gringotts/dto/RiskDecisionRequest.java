package com.gringotts.dto;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@NoArgsConstructor
@Getter
@Setter
public class RiskDecisionRequest {

    // Correlation
    @NotNull(message = "No Null Values")
    private UUID transactionId;

    // Actor
    @NotNull(message = "No Null Values")
    private Long userId;

    // Monetary
    @NotNull(message = "No Null Values")
    private BigDecimal amount;

    private BigDecimal totalAmountLast24h;

    private Integer txnCountLast24h;

    // Business intent
    @NotNull(message = "No Null Values")
    private TransactionType transactionType;

    // Execution channel
    @NotNull(message = "No Null Values")
    private Channel channel;

    // Accounts
    @NotNull(message = "No Null Values")
    private String sourceAccount;

    @NotNull(message = "No Null Values")
    private String targetAccount;

    // Contextual risk signals
    @NotNull(message = "No Null Values")
    private String country;

    @NotNull(message = "No Null Values")
    private String deviceId;

    // Event time (NOT DB time)
    @NotNull(message = "No Null Values")
    private Instant transactionTime;
}

