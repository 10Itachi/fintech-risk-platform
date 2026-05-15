package com.gringotts.transaction.transaction_service.domain.exception;

public class InvalidTransactionException extends BaseException{
    public InvalidTransactionException(String message) {
        super(message, "TXN_400_INVALID");
    }
}
