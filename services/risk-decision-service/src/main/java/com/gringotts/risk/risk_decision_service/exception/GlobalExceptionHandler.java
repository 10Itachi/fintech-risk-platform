package com.gringotts.risk.risk_decision_service.exception;

import io.swagger.v3.oas.annotations.extensions.Extensions;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String getCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : "N/A";
    }

    private ErrorResponse build(String code, String message, HttpStatus status) {
        return ErrorResponse.builder()
                .correlationId(getCorrelationId())
                .errorCode(code)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .build();
    }

    // ===============================
    // VALIDATION
    // ===============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(build("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST));
    }

    // ===============================
    // CONSTRAINT
    // ===============================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .distinct()
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(build("CONSTRAINT_VIOLATION", message, HttpStatus.BAD_REQUEST));
    }

    // ===============================
    // TYPE MISMATCH (FIXED)
    // ===============================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String msg = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());

        return ResponseEntity.badRequest()
                .body(build("TYPE_MISMATCH", msg, HttpStatus.BAD_REQUEST));
    }

    // ===============================
    // BUSINESS FAILURE (NEW)
    // ===============================
    @ExceptionHandler(RiskEvaluationException.class)
    public ResponseEntity<ErrorResponse> handleRiskEvaluation(RiskEvaluationException ex) {

        log.error("Risk evaluation failed. correlationId={}, type={}",
                getCorrelationId(), ex.getFailureType(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(build(
                        "RISK_EVALUATION_FAILED",
                        "Risk evaluation failed: " + ex.getFailureType().name(),
                        HttpStatus.SERVICE_UNAVAILABLE
                ));
    }

    @ExceptionHandler(DuplicateTransactionInProgressException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransactionInProgress(DuplicateTransactionInProgressException ex) {
        log.error("Duplicate transaction in progress. correlationId={}", getCorrelationId(), ex);

        return ResponseEntity.status(HttpStatus.PROCESSING)
                .body(build(
                        "TRANSACTION_IN_PROGRESS",
                                "Transaction is currently being processed",
                        HttpStatus.PROCESSING
                ));
    }

    // ===============================
    // NOT FOUND
    // ===============================
    @ExceptionHandler(RiskDecisionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RiskDecisionNotFoundException ex) {

        log.info("Resource not found. correlationId={}, message={}",
                getCorrelationId(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    // ===============================
    // SECURITY
    // ===============================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {

        log.warn("Access denied. correlationId={}", getCorrelationId());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(build("ACCESS_DENIED",
                        "You do not have permission to access this resource",
                        HttpStatus.FORBIDDEN));
    }

    // ===============================
    // GENERIC
    // ===============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        log.error("Unhandled exception occurred. correlationId={}",
                getCorrelationId(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build("INTERNAL_ERROR",
                        "Something went wrong. Please contact support.",
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }
}