package com.gringotts.userservice.user_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 🔹 1. Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {}", errors);

        return ResponseEntity.badRequest()
                .body(new ApiErrorDto(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        errors,
                        LocalDateTime.now()
                ));
    }

    // 🔹 2. Duplicate User
    @ExceptionHandler({
            UserAlreadyExistsException.class,
            UserAlreadyExistWithPhoneNumber.class
    })
    public ResponseEntity<ApiErrorDto> handleConflict(RuntimeException e) {

        log.warn("Conflict error: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorDto(
                        HttpStatus.CONFLICT,
                        e.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    // 🔹 3. Not Found
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(UserNotFound e) {

        log.warn("User not found: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto(
                        HttpStatus.NOT_FOUND,
                        e.getMessage(),
                        null,
                        LocalDateTime.now()
                ));
    }

    // 🔹 4. Service failure
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ApiErrorDto> handleServiceException(UserServiceException e) {

        log.error("Service failure: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDto(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal server error",
                        null,
                        LocalDateTime.now()
                ));
    }

    // 🔹 5. Security
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorDto> handleAuthException(Exception e) {

        log.warn("Authentication failure: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorDto(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials",
                        null,
                        LocalDateTime.now()
                ));
    }

    // 🔹 6. Fallback (critical)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGenericException(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDto(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Something went wrong",
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(IdentityProviderException.class)
    public ResponseEntity<ApiErrorDto> handleIdentityProviderException(IdentityProviderException ex) {

        log.error("Identity provider error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorDto(
                        HttpStatus.BAD_GATEWAY,
                        "Identity service error",
                        null,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorDto(
                        HttpStatus.FORBIDDEN,
                        "Access denied",
                        null,
                        LocalDateTime.now()
                ));
    }

}

/**
 * GLOBAL EXCEPTION HANDLER (The Safety Net)
 * -----------------------------------------
 * This class centralizes all error handling for the microservice.
 * It intercepts exceptions thrown from the Service or Controller layers
 * and transforms them into a standardized 'ApiErrorDto' response.
 *
 * It handles both framework-level errors (Validation, Security) and
 * domain-specific custom errors (UserNotFound, Conflict).
 */