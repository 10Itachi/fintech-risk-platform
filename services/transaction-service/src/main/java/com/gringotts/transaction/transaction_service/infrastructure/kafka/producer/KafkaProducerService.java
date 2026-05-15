package com.gringotts.transaction.transaction_service.infrastructure.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service


// this class create and send event
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String TOPIC = "transaction.finalized.v1";

    public void send(String key, String payload) {
        try {
            kafkaTemplate.send(TOPIC, key, payload).get(5, TimeUnit.SECONDS); //wait for 5 sec to see if it failed or not
        }
        catch (Exception e) {
            throw new RuntimeException("Kafka publish failed",e);
        }
    }
}
