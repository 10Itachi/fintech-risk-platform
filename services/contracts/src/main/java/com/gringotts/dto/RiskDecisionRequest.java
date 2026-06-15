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
        @NotNull
        private UUID transactionId;

        // Actor (from JWT → Keycloak sub)
        @NotNull
        private String userId;

        // Monetary
        @NotNull
        private BigDecimal amount;

        // Historical snapshots (optional enrichment)
        private BigDecimal totalAmountLast24h;
        private Integer txnCountLast24h;

        // Business intent
        @NotNull
        private TransactionType transactionType;

        // Execution channel
        @NotNull
        private Channel channel;

        // Accounts
        @NotNull
        private String sourceAccount;

        private String targetAccount; // nullable (withdrawal case)

        // Contextual signals
        @NotNull
        private String country;

        @NotNull
        private String deviceId;

        // Event time
        @NotNull
        private Instant transactionTime;
    }

