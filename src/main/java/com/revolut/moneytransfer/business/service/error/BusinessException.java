package com.revolut.moneytransfer.business.service.error;

public class BusinessException extends RuntimeException {
    private BusinessFailure businessFailure;

    public BusinessException(BusinessFailure businessFailure) {
        this.businessFailure = businessFailure;
    }

    public BusinessFailure getBusinessFailure() {
        return businessFailure;
    }
}
