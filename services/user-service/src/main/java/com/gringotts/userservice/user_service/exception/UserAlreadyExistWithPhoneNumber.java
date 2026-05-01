package com.gringotts.userservice.user_service.exception;

public class UserAlreadyExistWithPhoneNumber extends RuntimeException{
    public UserAlreadyExistWithPhoneNumber(String message) {
        super(message);
    }
}
