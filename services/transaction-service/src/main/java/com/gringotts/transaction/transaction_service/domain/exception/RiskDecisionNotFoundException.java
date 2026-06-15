package com.gringotts.transaction.transaction_service.domain.exception;

public class RiskDecisionNotFoundException extends BaseException {
    public RiskDecisionNotFoundException(String message) {
        super("TXN_404_RISK_NOT_FOUND", message);
    }
}
