package com.gringotts.gateway_service.exception;

import java.time.Instant;

public class ErrorResponse {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String correlationId;

    public ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String correlationId
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}