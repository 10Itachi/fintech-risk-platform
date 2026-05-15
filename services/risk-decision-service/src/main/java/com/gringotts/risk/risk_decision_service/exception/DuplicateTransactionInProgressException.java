package com.gringotts.risk.risk_decision_service.exception;

public class DuplicateTransactionInProgressException extends RuntimeException {
  public DuplicateTransactionInProgressException(String message) {
    super(message);
  }
}
