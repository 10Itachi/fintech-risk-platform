package com.gringotts.transaction.transaction_service.domain.exception;

public class RiskServiceException extends BaseException{
    public RiskServiceException(String message) {
        super(message, "TXN_503_RISK_SERVICE");
    }
}
