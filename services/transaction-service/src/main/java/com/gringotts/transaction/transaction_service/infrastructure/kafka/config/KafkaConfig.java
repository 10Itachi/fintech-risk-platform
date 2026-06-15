package com.gringotts.transaction.transaction_service.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

// this class is used to create kafka topic and its partitions
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicTransaction() {
        return  TopicBuilder.name("transaction.finalized.v1")
                .partitions(3) // increase for through put
                .replicas(3)  // one copy in each broker
                .configs(Map.of("min.insync.replicas","2")) // min 2 replicas must acknowledge the write was successful
                .build();
    }
}
