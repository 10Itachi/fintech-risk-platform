package com.gringotts.transaction.transaction_service.dto.response;

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

    private UUID transactionId;
    private String userName;
    private BigDecimal amount;
    private String sourceAccount;
    private String targetAccount;
    private TransactionType transactionType;
    private Channel channel;
    private String country;
    private String deviceId;
    private Instant transactionTime;
    private Instant createdAt;
    private TransactionStatus transactionStatus;
}