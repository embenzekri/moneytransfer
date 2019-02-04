package com.revolut.moneytransfer.business.service.error;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, RuntimeException exception) {
        super(message, exception);
    }
}
