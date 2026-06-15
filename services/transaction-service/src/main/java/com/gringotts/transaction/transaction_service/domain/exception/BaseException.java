package com.gringotts.transaction.transaction_service.domain.exception;

public abstract class BaseException extends RuntimeException {
    private final String errorCode; //private can not be changed externally

    protected BaseException(String message,String errorCode) { //protected only available for subclass
        super(message);
        this.errorCode = errorCode;
    }
    public String getErrorCode() {
        return errorCode;
    }
}
/*NOT a real exception
ONLY a template
Cannot instantiate directly ✔
Forces subclass usage ✔
*/