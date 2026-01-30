package com.gringotts.transaction.transaction_service.api.dto.response;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;
import com.gringotts.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class TransactionResponseDto {

    // Correlation
    private UUID transactionId;

    // Monetary
    private BigDecimal amount;

    // Accounts
    private String sourceAccount;
    private String targetAccount;

    // Business intent
    private TransactionType transactionType;

    // Execution context
    private Channel channel;

    private Instant createdAt;

    private TransactionStatus transactionStatus;
}
