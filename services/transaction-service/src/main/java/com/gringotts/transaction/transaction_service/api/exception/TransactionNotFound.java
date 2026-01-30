package com.gringotts.transaction.transaction_service.api.exception;

public class TransactionNotFound extends RuntimeException{
    public TransactionNotFound(String message) {
        super(message);
    }
}
