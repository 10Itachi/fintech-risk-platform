package com.gringotts.transaction.transaction_service.api.controller;

import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transaction.transaction_service.infrastructure.kafka.producer.TransactionEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/kafka-test")
public class KafkaTestController {

    private final TransactionEventProducer transactionEventProducer;

    public KafkaTestController(TransactionEventProducer transactionEventProducer) {
        this.transactionEventProducer = transactionEventProducer;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishEvent(@RequestBody TransactionFinalizedEvent event) {
        transactionEventProducer.publish(event);
        return ResponseEntity.accepted().build();
    }
}
