package com.gringotts.transactionobservability.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {

        log.warn("API_ERROR code={} msg={} path={}",
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.builder()
                        .errorCode(ex.getErrorCode())
                        .message(ex.getMessage())
                        .timestamp(java.time.Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("UNEXPECTED_ERROR path={} msg={}",
                request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(500)
                .body(ErrorResponse.builder()
                        .errorCode("INTERNAL_SERVER_ERROR")
                        .message("Something went wrong")
                        .timestamp(java.time.Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }
}