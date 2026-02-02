package com.gringotts.transaction.transaction_service.infrastructure.kafka.producer;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publish(TransactionFinalizedEvent event) {
        kafkaTemplate.send(
                "transaction.finalized.v1",
                event.transactionId().toString(),
                event
        );
    }
}
