package com.revolut.moneytransfer.business.service.error;

public class TechnicalException extends RuntimeException {
    public TechnicalException(String message, RuntimeException exception) {
        super(message, exception);
    }
}
