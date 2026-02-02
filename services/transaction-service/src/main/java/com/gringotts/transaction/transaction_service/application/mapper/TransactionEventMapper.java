package com.gringotts.transaction.transaction_service.application.mapper;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.kafkaevents.TransactionFinalizedEvent;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionEventMapper {

    public TransactionFinalizedEvent toEvent(Transaction transaction, RiskDecisionResponse riskDecisionResponse) {
        return   new TransactionFinalizedEvent(
                transaction.getTransactionId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getChannel(),
                transaction.getCountry(),
                transaction.getTransactionStatus(),
                riskDecisionResponse.getRiskScore(),
                riskDecisionResponse.getFraudProbability(),
                riskDecisionResponse.getReasonCode(),
                riskDecisionResponse.getPolicyVersion(),
                riskDecisionResponse.getModelVersion(),
                Instant.now()
                );
    }
}
