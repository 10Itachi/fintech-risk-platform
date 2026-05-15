package com.gringotts.transaction.transaction_service.domain.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // HANDLE BASE EXCEPTIONS (CUSTOM)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiErrorDto> handleBaseException(
            BaseException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = mapToHttpStatus(ex);

        log.warn("Business exception occurred | errorCode={} | message={} | path={}",
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        ApiErrorDto error = ApiErrorDto.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, status);
    }

    // =========================
    // HANDLE VALIDATION ERRORS (@Valid)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed | message={} | path={}", message, request.getRequestURI());

        ApiErrorDto error = ApiErrorDto.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("TXN_400_VALIDATION_ERROR")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================
    // HANDLE CONSTRAINT VIOLATION (e.g. @RequestParam)
    // =========================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        log.warn("Constraint violation | message={} | path={}", ex.getMessage(), request.getRequestURI());

        ApiErrorDto error = ApiErrorDto.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("TXN_400_CONSTRAINT_VIOLATION")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =========================
    // HANDLE GENERIC EXCEPTION (FALLBACK)
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unexpected error occurred | message={} | path={}",
                ex.getMessage(), request.getRequestURI(), ex);

        ApiErrorDto error = ApiErrorDto.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .errorCode("TXN_500_INTERNAL_ERROR")
                .message("Unexpected error occurred")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // MAP CUSTOM ERROR CODES → HTTP STATUS
    private HttpStatus mapToHttpStatus(BaseException ex) {

        String code = ex.getErrorCode();

        if (code.startsWith("TXN_400")) {
            return HttpStatus.BAD_REQUEST;
        }

        if (code.startsWith("TXN_404")) {
            return HttpStatus.NOT_FOUND;
        }

        if (code.startsWith("TXN_409")) {
            return HttpStatus.CONFLICT;
        }

        if (code.startsWith("TXN_503")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        if (code.contains("TXN_500_SERIALIZATION")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if(code.startsWith("TXN_409_PROCESSING")) {
            return HttpStatus.CONFLICT;
        }

        if (code.contains("TXN_404_RISK_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        if (code.contains("TXN_401_USER_CONTEXT")) {
            return HttpStatus.UNAUTHORIZED;
        }

        return HttpStatus.CONFLICT;
    }
}