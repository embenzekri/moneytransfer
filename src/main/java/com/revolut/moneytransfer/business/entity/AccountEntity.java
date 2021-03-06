package com.revolut.moneytransfer.business.entity;

import com.revolut.moneytransfer.business.service.IDGenerator;

import java.math.BigDecimal;

public class AccountEntity extends Entity {

    private String name;
    private BigDecimal balance;
    private String currency;
    private State state;

    public AccountEntity(String name, BigDecimal balance, String currency) {
        this(IDGenerator.generateAccountId(), name, balance, currency, State.ACTIVE);
    }

    public AccountEntity(String id, String name, BigDecimal balance, String currency, State state) {
        super(id);
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public State getState() {
        return state;
    }

    public AccountEntity credit(BigDecimal amount) {
        return new AccountEntity(getId(), name, balance.add(amount), currency, state);
    }

    public AccountEntity debit(BigDecimal amount) {
        return new AccountEntity(getId(), name, balance.subtract(amount), currency, state);
    }

    public AccountEntity deactivate() {
        return new AccountEntity(getId(), name, balance, currency, State.INACTIVE);
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public enum State {
        ACTIVE, INACTIVE
    }
}


