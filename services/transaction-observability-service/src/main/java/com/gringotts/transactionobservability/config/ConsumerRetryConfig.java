package com.gringotts.transactionobservability.config;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class ConsumerRetryConfig {

    /**
     * Global Error Handler for non-retryable technical failures.
     * Note: @RetryableTopic on the listener will take precedence
     * for business-level retries.
     */
        @Bean
        public KafkaTemplate<Object, Object> dlqProducerTemplate(ProducerFactory<Object, Object> pf) {
            // Industry Standard: Use a delegating serializer to handle mixed types
            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                    pf.getConfigurationProperties(),
                    new DelegatingByTypeSerializer(Map.of(
                            byte[].class, new ByteArraySerializer(),
                            String.class, new StringSerializer()
                    )),
                    new DelegatingByTypeSerializer(Map.of(
                            byte[].class, new ByteArraySerializer(),
                            String.class, new StringSerializer()
                    ))
            ));
        }

        @Bean
        public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> dlqProducerTemplate) {

            // 1. Configure the Recoverer with custom naming strategy (-dlt)
            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dlqProducerTemplate,
                    (record, ex) -> new TopicPartition(record.topic() + "-dlt", record.partition()));

            // 2. Define Backoff: 3 retries, 2-second interval
            FixedBackOff backOff = new FixedBackOff(2000L, 3);

            DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

            // 3. Industry Standard: Do not retry errors that will never succeed
            handler.addNotRetryableExceptions(
                    DeserializationException.class, // Prevents the infinite loop you saw
                    MessageConversionException.class,
                    IllegalArgumentException.class
            );

            // 4. Log failures for observability
            handler.setLogLevel(KafkaException.Level.ERROR);

            return handler;
        }
    }

