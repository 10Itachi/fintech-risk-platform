package com.gringotts.userservice.user_service.exception;

import com.gringotts.userservice.user_service.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 🔹 1. Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {}", errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                errors
        );
    }

    // 🔹 2. User Not Found
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(UserNotFound ex) {

        log.warn("User not found: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ErrorCode.USER_NOT_FOUND,
                ex.getMessage(),
                null
        );
    }

    // 🔹 3. Duplicate User
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDto> handleUserAlreadyExists(UserAlreadyExistsException ex) {

        log.warn("User already exists: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.CONFLICT,
                ErrorCode.USER_ALREADY_EXISTS,
                ex.getMessage(),
                null
        );
    }

    // 🔹 4. Identity Provider (Keycloak)
    @ExceptionHandler(IdentityProviderException.class)
    public ResponseEntity<ApiErrorDto> handleIdentityProviderException(IdentityProviderException ex) {

        log.error("Identity provider error: {}", ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                ErrorCode.IDENTITY_PROVIDER_ERROR,
                ex.getMessage(),
                null
        );
    }

    // 🔹 5. Authentication
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorDto> handleAuthException(Exception ex) {

        log.warn("Authentication failure: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.AUTHENTICATION_FAILED,
                "Invalid credentials",
                null
        );
    }

    // 🔹 6. Authorization
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "Access denied",
                null
        );
    }

    // 🔹 7. Business Service Failure
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ApiErrorDto> handleServiceException(UserServiceException ex) {

        log.error("Service failure: {}", ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage(),   // ⚠️ Keep message for debugging clarity
                null
        );
    }

    // 🔹 8. Fallback (last safety net)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGenericException(Exception ex) {

        log.error("Unexpected error occurred", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                null
        );
    }

    // 🔹 Common Builder Method (keeps everything consistent)
    private ResponseEntity<ApiErrorDto> buildResponse(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Object details
    ) {
        return ResponseEntity.status(status)
                .body(new ApiErrorDto(
                        status.value(),
                        errorCode.name(),
                        message,
                        details,
                        LocalDateTime.now()
                ));
    }
}