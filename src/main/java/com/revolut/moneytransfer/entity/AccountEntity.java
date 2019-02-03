package com.revolut.moneytransfer.entity;

import java.math.BigDecimal;

public class AccountEntity extends Entity {

    private String name = null;
    private BigDecimal balance = null;
    private String currency = null;
    private String state = null;

    public AccountEntity(String id, String name, BigDecimal balance, String currency, String state) {
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

    public String getState() {
        return state;
    }

    public AccountEntity credit(BigDecimal amount) {
        return new AccountEntity(getId(), name, balance.add(amount), currency, state);
    }


    public AccountEntity debit(BigDecimal amount) {
        return new AccountEntity(getId(), name, balance.subtract(amount), currency, state);
    }
}
