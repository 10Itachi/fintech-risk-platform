package com.gringotts.userservice.user_service.exception;

public class UserListEmpty extends RuntimeException {
    public UserListEmpty(String message) {
        super(message);
    }
}
