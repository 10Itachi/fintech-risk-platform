package com.gringotts.kafkaevents;

import com.gringotts.enums.Channel;
import com.gringotts.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionFinalizedEvent(
        UUID transactionId,
        Long userId,
        BigDecimal amount,
        Channel channel,
        String country,
        TransactionStatus finalStatus,
        // risk snapshot
        Integer riskScore,
        Double fraudProbability,
        List<String> reasonCodes,
        // governance
        String policyVersion,
        String modelVersion,
        Instant occurredAt
) {

}