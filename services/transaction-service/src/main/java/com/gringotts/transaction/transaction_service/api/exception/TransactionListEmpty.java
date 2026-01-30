package com.gringotts.transaction.transaction_service.api.exception;

public class TransactionListEmpty extends RuntimeException {
    public TransactionListEmpty(String message) {
        super(message);
    }
}
