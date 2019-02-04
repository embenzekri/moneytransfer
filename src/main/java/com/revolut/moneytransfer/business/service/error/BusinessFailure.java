package com.revolut.moneytransfer.business.service.error;

public enum BusinessFailure {
    ACCOUNT_NOT_FOUND("Account not found"),
    TRANSFER_NOT_FOUND("Transfer not found"),
    ACCOUNT_STATE_INVALID("Account state is not valid and cannot proceed with the requested operation."),
    TRANSFER_AMOUNT_EXCEEDED("Transfer amount exceeds the source account balance."),
    TRANSFER_STATE_INVALID("The transfer state does not allow this operation.");

    private String message;

    BusinessFailure(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
