package com.gringotts.transaction.transaction_service.api.exception;

public class UserListEmpty extends RuntimeException {
    public UserListEmpty(String message) {
        super(message);
    }
}
