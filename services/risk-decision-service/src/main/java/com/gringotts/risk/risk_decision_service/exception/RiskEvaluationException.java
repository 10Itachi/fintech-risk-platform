package com.gringotts.risk.risk_decision_service.exception;

import com.gringotts.risk.risk_decision_service.enums.RiskFailureType;

public class RiskEvaluationException extends RuntimeException {

    private final RiskFailureType failureType;

    public RiskEvaluationException(RiskFailureType failureType, Throwable cause) {
        super(cause);
        this.failureType = failureType;
    }

    public RiskFailureType getFailureType() {
        return failureType;
    }
}