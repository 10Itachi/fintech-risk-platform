package com.gringotts.transaction.transaction_service.domain.exception;

public class InvalidUserContextException extends BaseException {
    public InvalidUserContextException(String message) {
        super(message,"TXN_401_USER_CONTEXT");
    }
}
