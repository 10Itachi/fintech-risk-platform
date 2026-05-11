package com.gringotts.userservice.user_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiErrorDto {

    private final int status;          // HTTP status code (e.g. 409)
    private final String error;        // MACHINE READABLE (e.g. USER_ALREADY_EXISTS)
    private final String message;      // HUMAN READABLE (e.g. Username already exists)
    private final Object details;      // Optional (validation errors, etc.)
    private final LocalDateTime timestamp;
}