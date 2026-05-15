package com.gringotts.transaction.transaction_service.domain.exception;

public class InvalidTransactionStateException extends BaseException{
    public InvalidTransactionStateException(String message) {
        super(message, "TXN_400_INVALID_STATE");
    }
}
