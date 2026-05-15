package com.gringotts.transaction.transaction_service.domain.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class OutboxSerializationException extends BaseException {
    public OutboxSerializationException(String message) {
        // Use a 500-series code for internal processing failures
        super(message,"TXN_500_SERIALIZATION");
    }
}
