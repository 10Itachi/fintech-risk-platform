package com.gringotts.ledger.transaction_event_ledger_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class ConsumerRetryConfig {

    @Bean
    public RetryTopicConfiguration retryTopicConfiguration(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${spring.kafka.topics.transaction-finalized}") String topic
    ) {

        return RetryTopicConfigurationBuilder
                .newInstance()

                // Retry strategy: transient failures
                .fixedBackOff(2_000)      // 2 seconds
                .maxAttempts(4)           // 1 original + 3 retries
                // Apply retry only to THIS topic
                .includeTopic(topic)
                // Route permanently failed messages to DLT
                .dltHandlerMethod(
                        "transactionEventConsumer",
                        "handle"
                )

                .create(kafkaTemplate);
    }

    @Bean
    public DefaultErrorHandler errorHandler() {

        FixedBackOff backOff = new FixedBackOff(0L, 0); // no in-memory retries

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        //These go DIRECTLY to DLT (no retry topics)
        handler.addNotRetryableExceptions(
                MessageConversionException.class,   // bad JSON / schema
                IllegalArgumentException.class      // validation errors
        );

        return handler;
    }
}
