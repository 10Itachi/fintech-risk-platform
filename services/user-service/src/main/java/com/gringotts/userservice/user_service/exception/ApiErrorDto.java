package com.gringotts.userservice.user_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorDto {

    private HttpStatus status;
    private String message;
    private Object details;        // 🔥 for validation errors / extra info
    private LocalDateTime timestamp; // 🔥 for tracing
}