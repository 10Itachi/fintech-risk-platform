package com.gringotts.transaction.transaction_service.domain.exception;

public class InvalidTransactionStateException extends RuntimeException {
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}
