package com.gringotts.notification_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
     =========================================================
     FAILED NOTIFICATION NOT FOUND
     =========================================================
     */
    @ExceptionHandler(
            FailedNotificationNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleFailedNotificationNotFound(

            FailedNotificationNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "event=FAILED_NOTIFICATION_NOT_FOUND message={}",
                ex.getMessage()
        );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .correlationId(
                                MDC.get("correlationId")
                        )
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /*
     =========================================================
     PROCESSED EVENT NOT FOUND
     =========================================================
     */
    @ExceptionHandler(
            NotificationEventNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleNotificationEventNotFound(

            NotificationEventNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "event=NOTIFICATION_EVENT_NOT_FOUND message={}",
                ex.getMessage()
        );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .correlationId(
                                MDC.get("correlationId")
                        )
                        .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /*
     =========================================================
     FALLBACK EXCEPTION
     =========================================================
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleGenericException(

            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "event=UNHANDLED_EXCEPTION path={} message={}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                        .error(
                                HttpStatus.INTERNAL_SERVER_ERROR
                                        .getReasonPhrase()
                        )
                        .message(
                                "Internal server error"
                        )
                        .path(request.getRequestURI())
                        .correlationId(
                                MDC.get("correlationId")
                        )
                        .build();

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(response);
    }
}