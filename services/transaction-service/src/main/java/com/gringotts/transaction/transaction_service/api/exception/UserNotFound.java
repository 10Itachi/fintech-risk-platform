package com.gringotts.transaction.transaction_service.api.exception;

public class UserNotFound extends RuntimeException {
    public UserNotFound(String message) {
            super(message);
        }
}
