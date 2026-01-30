package com.gringotts.transaction.transaction_service.api.dto.request;
import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
@Builder
public class TransactionRequestDto {

    @NotNull(message = "Enter the amount")
    @PositiveOrZero
    private BigDecimal amount;

    @NotNull(message = "Enter transaction type")
    private TransactionType transactionType;

    @NotBlank
    @Pattern(
            regexp = "^ACC-[A-Z]{2}-[0-9]{6,12}$",
            message = "Invalid account format. Use ACC-<COUNTRY>-<6–12 DIGITS>, e.g. ACC-IN-000123"
    )
    private String sourceAccount;
    @Pattern(
            regexp = "^ACC-[A-Z]{2}-[0-9]{6,12}$",
            message = "Invalid account format. Use ACC-<COUNTRY>-<6–12 DIGITS>, e.g. ACC-IN-000123"
    )
    private String targetAccount;


    @NotNull(message = "Enter the channel")
    private Channel channel;

    @NotBlank
    @Pattern(
            regexp = "^[A-Z]{2}$",
            message = "Country must be ISO-2 code"
    )
    private String country;

    @NotBlank
    @Pattern(
            regexp = "^DEV-(ANDROID|IOS|WEB)-[A-Z0-9]{5,20}$",
            message = "Invalid deviceId. Use DEV-<ANDROID|IOS|WEB>-<5–20 ALPHANUMERIC>, e.g. DEV-ANDROID-AB123"

            )
    private String deviceId;

}
