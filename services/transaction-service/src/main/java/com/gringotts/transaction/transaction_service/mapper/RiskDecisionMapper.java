package com.gringotts.transaction.transaction_service.mapper;

import com.gringotts.dto.RiskDecisionResponse;
import com.gringotts.transaction.transaction_service.domain.model.RiskDecision;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskDecisionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionId", source = "transaction.transactionId")
    @Mapping(target = "decision", source = "response.transactionStatus")
    @Mapping(target = "riskScore", source = "response.riskScore")
    @Mapping(target = "fraudProbability", source = "response.fraudProbability")
    @Mapping(target = "reasonCodes", source = "response.reasonCodes")
    @Mapping(target = "modelName", source = "response.modelName")
    @Mapping(target = "modelVersion", source = "response.modelVersion")
    @Mapping(target = "policyVersion", source = "response.policyVersion")
    @Mapping(target = "evaluatedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "latencyMs", source = "latency")
    RiskDecision toEntity(Transaction transaction,
                          RiskDecisionResponse response,
                          long latency);
}
