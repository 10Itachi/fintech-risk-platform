package com.gringotts.ledger.transaction_event_ledger_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class ConsumerRetryConfig {

    /**
     * Global Error Handler for non-retryable technical failures.
     * Note: @RetryableTopic on the listener will take precedence
     * for business-level retries.
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        // 0 retries here because @RetryableTopic handles the retry logic via Kafka topics
        FixedBackOff backOff = new FixedBackOff(0L, 0);

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        // Immediate DLT routing for "Poison Pills" (Bad Data)
        handler.addNotRetryableExceptions(
                MessageConversionException.class,
                IllegalArgumentException.class
        );

        return handler;
    }
}