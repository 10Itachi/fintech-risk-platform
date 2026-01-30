package com.gringotts.transaction.transaction_service.application.mapper;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface RiskRequestMapper {

    @Mapping(target ="transactionId", source = "transaction.transactionId")
    @Mapping(target ="userId", source = "transaction.userId")
    @Mapping(target ="amount", source = "transaction.amount")
    @Mapping(target ="transactionType", source = "transaction.transactionType")
    @Mapping(target ="channel", source = "transaction.channel")
    @Mapping(target ="sourceAccount", source = "transaction.sourceAccount")
    @Mapping(target ="targetAccount", source = "transaction.targetAccount")
    @Mapping(target ="country", source = "transaction.country")
    @Mapping(target ="deviceId", source = "transaction.deviceId")

    @Mapping(target = "totalAmountLast24h", source = "totalAmountLast24h")
    @Mapping(target = "txnCountLast24h", source = "txnCountLast24h")
    @Mapping(target = "transactionTime", source = "transactionTime")

    RiskDecisionRequest toRiskRequest(
            Transaction transaction,
            BigDecimal totalAmountLast24h,
            Integer txnCountLast24h,
            Instant transactionTime
    );
}
