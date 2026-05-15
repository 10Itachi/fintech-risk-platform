package com.gringotts.risk.risk_decision_service.enums;

public enum RiskFailureType {
    ML_FAILURE,
    SOFT_RULE_FAILURE,
    HARD_RULE_FAILURE, // rare but possible
    ML_TIMEOUT, SOFT_RULE_TIMEOUT, UNKNOWN_FAILURE
}
