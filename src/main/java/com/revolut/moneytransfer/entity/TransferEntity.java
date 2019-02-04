package com.revolut.moneytransfer.entity;

import java.math.BigDecimal;

import static com.revolut.moneytransfer.entity.TransferEntity.State.CANCELED;
import static com.revolut.moneytransfer.entity.TransferEntity.State.COMPLETED;
import static com.revolut.moneytransfer.entity.TransferEntity.State.FAILED;

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

    public TransferEntity executed() {
        return new TransferEntity(getId(), fromAccountId, toAccountId, amount, currency, COMPLETED);
    }

    public TransferEntity canceled() {
        return new TransferEntity(getId(), fromAccountId, toAccountId, amount, currency, CANCELED);
    }

    public TransferEntity failed() {
        return new TransferEntity(getId(), fromAccountId, toAccountId, amount, currency, FAILED);
    }

    public enum State {
        PENDING, COMPLETED, CANCELED, FAILED
    }
}
