package com.gringotts.transaction.transaction_service.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->fieldError.getField()+" : "+fieldError.getDefaultMessage())
                .toList();
        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.BAD_REQUEST, String.valueOf(errors));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistWithPhoneNumber.class)
    public ResponseEntity<?>handleUserAlreadyExistWithPhoneNumber(UserAlreadyExistWithPhoneNumber e){
        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<?>handleUserNotFound(UserNotFound e){
        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserListEmpty.class)
    public ResponseEntity<?>handleUserListEmpty(UserListEmpty e){
        ApiErrorDto errorDto = new ApiErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGenericException(Exception ex) {
        ApiErrorDto error = new ApiErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(TransactionListEmpty.class)
    public ResponseEntity<?>handleTransactionListEmpty(TransactionListEmpty e){
        ApiErrorDto error = new ApiErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(TransactionNotFound.class)
    public ResponseEntity<?>handleTransactionNotFound(TransactionNotFound e){
        ApiErrorDto error = new ApiErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?>handleBadCredentialsException(BadCredentialsException e){
        ApiErrorDto error = new ApiErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?>handleAuthenticationException(AuthenticationException e){
        ApiErrorDto error = new ApiErrorDto(HttpStatus.UNAUTHORIZED, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RiskServiceException.class)
    public ResponseEntity<?>handleRiskServiceException(RiskServiceException e){
        ApiErrorDto error = new ApiErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
