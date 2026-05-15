package com.gringotts.transaction.transaction_service.domain.exception;

public class TransactionNotFoundException extends BaseException {
    public TransactionNotFoundException(String message) {
        super(message, "TXN_404_NOT_FOUND");
    }
}
