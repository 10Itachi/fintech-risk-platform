package com.gringotts.transaction.transaction_service.api.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Builder
@Getter
@AllArgsConstructor
@Setter
public class ApiErrorDto {
    private HttpStatus status;
    private String message;
}
