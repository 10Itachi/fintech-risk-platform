package com.gringotts.transactionobservability.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final Instant timestamp;
    private final String path;
}