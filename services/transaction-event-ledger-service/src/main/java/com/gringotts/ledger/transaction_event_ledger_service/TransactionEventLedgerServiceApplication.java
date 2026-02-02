package com.gringotts.ledger.transaction_event_ledger_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication/*
@EntityScan(basePackages = "com.gringotts.ledger.transaction_event_ledger_service.domain.model")
@EnableJpaRepositories(basePackages = "com.gringotts.ledger.transaction_event_ledger_service")*/

public class TransactionEventLedgerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionEventLedgerServiceApplication.class, args);
	}

}
