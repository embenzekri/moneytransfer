package com.revolut.moneytransfer.entity;

import java.math.BigDecimal;

public class TransferEntity extends Entity {

    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private State state;

    public TransferEntity(String id, String fromAccountId, String toAccountId, BigDecimal amount, String currency, State state) {
        super(id);
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
        this.state = state;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public State getState() {
        return state;
    }

    public enum State {
        PENDING, COMPLETED, CANCELED
    }
}
