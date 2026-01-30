package com.gringotts.transaction.transaction_service.application.service;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.transaction.transaction_service.infrastructure.client.RiskServiceClient;
import feign.FeignException;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RiskOrchestrationService {
    private final Logger LOGGER = LoggerFactory.getLogger(RiskOrchestrationService.class);
    private final RiskServiceClient riskServiceClient;

    public RiskOrchestrationService(RiskServiceClient riskServiceClient) {
        this.riskServiceClient = riskServiceClient;
    }

    public RiskDecisionResponse evaluateRiskIndependent(RiskDecisionRequest riskDecisionRequest) {
        try {
            return riskServiceClient.evaluateRisk(riskDecisionRequest);
        }catch (RetryableException ex){
            LOGGER.warn("Risk service timeout. TransactionId={}" , riskDecisionRequest.getTransactionId());
            return RiskDecisionResponse.reviewFallback();
        }catch (FeignException ex){
            if (ex.status() >= 500) {
                LOGGER.warn("Risk service timeout. TransactionId={}" , riskDecisionRequest.getTransactionId());
                return RiskDecisionResponse.reviewFallback();
            }
            throw ex;
        }

    }
}
