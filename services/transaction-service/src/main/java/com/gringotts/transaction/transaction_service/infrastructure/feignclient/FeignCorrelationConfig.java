package com.gringotts.transaction.transaction_service.infrastructure.feignclient;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignCorrelationConfig {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Bean
    public RequestInterceptor correlationIdInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {

                String correlationId = MDC.get("X-Correlation-ID");

                if (correlationId != null && !correlationId.isBlank()) {
                    template.header(CORRELATION_ID_HEADER, correlationId);
                }
            }
        };
    }
}