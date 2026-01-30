package com.gringotts.transaction.transaction_service.api.exception;

public class UserAlreadyExistWithPhoneNumber extends RuntimeException{
    public UserAlreadyExistWithPhoneNumber(String message) {
        super(message);
    }
}
