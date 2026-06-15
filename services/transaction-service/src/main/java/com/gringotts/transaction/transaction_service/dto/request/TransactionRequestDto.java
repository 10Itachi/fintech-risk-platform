package com.gringotts.transaction.transaction_service.dto.request;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Builder
public class TransactionRequestDto {

    // =========================
    // Monetary
    // =========================

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    // =========================
    // Business Intent
    // =========================

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    // =========================
    // Accounts
    // =========================

    @NotBlank(message = "Source account is required")
    @Pattern(
            regexp = "^ACC-[A-Z]{2,3}-[0-9]{6,12}$",
            message = "Invalid account format. Example: ACC-IN-000123"
    )
    private String sourceAccount;

    // Nullable → depends on transactionType (validated in service layer)
    @Pattern(
            regexp = "^ACC-[A-Z]{2,3}-[0-9]{6,12}$",
            message = "Invalid account format. Example: ACC-IN-000123"
    )
    private String targetAccount;

    // =========================
    // Execution Context
    // =========================

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotBlank(message = "Country is required")
    @Pattern(
            regexp = "^[A-Z]{2,3}$",
            message = "Country must be ISO code (2 or 3 letters)"
    )
    private String country;

    @NotBlank(message = "DeviceId is required")
    @Pattern(
            regexp = "^DEV-(ANDROID|IOS|WEB)-[A-Z0-9]{5,20}$",
            message = "Invalid deviceId format"
    )
    private String deviceId;

    // =========================
    // Business Event Time
    // =========================

    @NotNull(message = "Transaction time is required")
    private Instant transactionTime;

}