package com.gringotts.transaction.transaction_service.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;


@Getter
@Builder
@AllArgsConstructor
public class ApiErrorDto {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String errorCode;
    private final String message;
    private final String path;
}
