package com.gringotts.risk.risk_decision_service.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String correlationId;

    private final String errorCode;

    private final String message;

    private final int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;
}