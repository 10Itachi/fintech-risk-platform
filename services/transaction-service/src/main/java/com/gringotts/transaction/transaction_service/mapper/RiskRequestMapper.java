package com.gringotts.transaction.transaction_service.mapper;

import com.gringotts.dto.RiskDecisionRequest;
import com.gringotts.transaction.transaction_service.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;

@Mapper(componentModel = "spring")
public interface RiskRequestMapper {

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "sourceAccount", source = "sourceAccount")
    @Mapping(target = "targetAccount", source = "targetAccount")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "transactionTime", source = "transactionTime")

    // Snapshots come from entity (already stored)
    @Mapping(target = "totalAmountLast24h", source = "totalAmountLast24h")
    @Mapping(target = "txnCountLast24h", source = "txnCountLast24h")

    RiskDecisionRequest toRiskRequest(Transaction transaction);
}
