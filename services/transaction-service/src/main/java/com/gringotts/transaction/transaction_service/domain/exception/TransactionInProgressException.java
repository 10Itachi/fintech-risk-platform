package com.gringotts.transaction.transaction_service.domain.exception;

public class TransactionInProgressException extends BaseException {
    public TransactionInProgressException(String message) {
        // "TXN_409_PROCESSING" is the code, 'message' is the detail
        super("TXN_409_PROCESSING", message);
    }
}
