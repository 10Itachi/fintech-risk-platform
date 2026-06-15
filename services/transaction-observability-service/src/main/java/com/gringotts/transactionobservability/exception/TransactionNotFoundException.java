package com.gringotts.transactionobservability.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TransactionNotFoundException extends ApiException {

    public TransactionNotFoundException(UUID txnId) {
        super(
                "transaction entry not found for transactionId=" + txnId,
                "TRANSACTION_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}
