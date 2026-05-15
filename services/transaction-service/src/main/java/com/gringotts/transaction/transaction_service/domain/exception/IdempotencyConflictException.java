package com.gringotts.transaction.transaction_service.domain.exception;

public class IdempotencyConflictException extends BaseException {
    public IdempotencyConflictException(String message) {
        super(message,"TXN_409_IDEMPOTENCY_CONFLICT");
    }
}
