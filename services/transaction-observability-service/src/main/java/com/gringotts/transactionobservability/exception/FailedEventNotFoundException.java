package com.gringotts.transactionobservability.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class FailedEventNotFoundException extends ApiException {

    public FailedEventNotFoundException(UUID id) {
        super(
                "Failed event not found id=" + id,
                "FAILED_EVENT_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}
