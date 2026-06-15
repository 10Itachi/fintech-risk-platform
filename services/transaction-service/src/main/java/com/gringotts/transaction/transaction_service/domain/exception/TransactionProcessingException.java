package com.gringotts.transaction.transaction_service.domain.exception;

public class TransactionProcessingException extends BaseException{
    public TransactionProcessingException(String message) {
        super(message, "TXN_500_PROCESSING_ERROR");
    }
}
