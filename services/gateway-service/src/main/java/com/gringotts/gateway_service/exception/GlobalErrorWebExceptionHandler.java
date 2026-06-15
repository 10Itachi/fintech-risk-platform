package com.gringotts.gateway_service.exception;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;

import org.springframework.core.annotation.Order;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler
        implements ErrorWebExceptionHandler {

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-ID";

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
                             Throwable ex) {

        HttpStatus status =
                HttpStatus.INTERNAL_SERVER_ERROR;

        String message =
                "Internal server error";

        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_ID_HEADER);

        ErrorResponse errorResponse =
                new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        correlationId
                );

        exchange.getResponse()
                .setStatusCode(status);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {

            byte[] responseBytes =
                    objectMapper.writeValueAsBytes(
                            errorResponse
                    );

            return exchange.getResponse()
                    .writeWith(
                            Mono.just(
                                    exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(responseBytes)
                            )
                    );

        } catch (Exception e) {

            return Mono.error(e);
        }
    }
}