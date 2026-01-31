package com.gringotts.transaction.transaction_service.application.mapper;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import com.gringotts.transaction.transaction_service.domain.model.TransactionFinalizedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionEventMapper {

    public TransactionFinalizedEvent toEvent(Transaction transaction, RiskDecisionResponse riskDecisionResponse) {
        return  TransactionFinalizedEvent.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .channel(transaction.getChannel())
                .country(transaction.getCountry())
                .finalStatus(transaction.getTransactionStatus())
                .riskScore(riskDecisionResponse.getRiskScore())
                .fraudProbability(riskDecisionResponse.getFraudProbability())
                .reasonCodes(riskDecisionResponse.getReasonCode())
                .policyVersion(riskDecisionResponse.getPolicyVersion())
                .modelVersion(riskDecisionResponse.getModelVersion())
                .occurredAt(Instant.now())
                .build();
    }

}
