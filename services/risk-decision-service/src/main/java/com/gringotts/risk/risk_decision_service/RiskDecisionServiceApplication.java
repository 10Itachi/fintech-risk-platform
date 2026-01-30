package com.gringotts.risk.risk_decision_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.gringotts")
public class RiskDecisionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiskDecisionServiceApplication.class, args);
	}

}
