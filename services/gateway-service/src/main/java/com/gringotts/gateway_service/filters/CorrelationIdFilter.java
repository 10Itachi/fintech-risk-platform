package com.gringotts.gateway_service.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String CORRELATION_ID_HEADER = "X-Correlation-ID";
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);
        if(correlationId == null || correlationId.isBlank()){
            correlationId = "Co-relationId:"+ UUID.randomUUID();
        }
        // Add to request headers
        ServerHttpRequest mutatedRequest =
                exchange.getRequest()
                        .mutate()
                        .header(CORRELATION_ID_HEADER, correlationId)
                        .build();

        // Add to MDC for logging
        MDC.put(CORRELATION_ID_HEADER, correlationId);
        log.info("CorrelationId is {}", correlationId);
        return chain.filter(
                        exchange.mutate()
                                .request(mutatedRequest)
                                .build()
                )
                .doFinally(signal -> MDC.clear());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
