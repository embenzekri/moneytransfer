package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.Accounts;
import com.revolut.moneytransfer.model.CreateAccountRequest;

import java.math.BigDecimal;
import java.util.Optional;

public class AccountService {
    private IDGenerator idGenerator = new IDGenerator();
    private Accounts accountList = new Accounts();
    
    public AccountService() {
        accountList.add(new Account("/accounts/3fc6b414-cdb8-4b8f-beb5-fb08c2902f87", "account1", new BigDecimal(1000), "EUR", "ACTIVE"));
        accountList.add(new Account("/accounts/9aecab5d-3827-4624-97a9-11b1207c7a12", "account2", new BigDecimal(5000), "EUR", "ACTIVE"));
        accountList.add(new Account("/accounts/e9ccb93b-bded-41a3-8e7e-95c3a322a8ee", "account3", new BigDecimal(7000), "EUR", "ACTIVE"));
    }

    public Accounts listAccounts() {
        return accountList;
    }

    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account(idGenerator.generateAccountId(), request.getName(), request.getBalance(), request.getCurrency(), "ACTIVE");
        accountList.add(account);
        return account;
    }

    public Optional<Account> getAccount(String fromAccountId) {
        for (Account account : accountList) {
            if (account.equals(fromAccountId)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }

    public boolean debit(Account fromAccount, BigDecimal amount) {
        BigDecimal balance = fromAccount.getBalance();
        if (balance.floatValue() >= amount.floatValue()) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            return true;
        }
        return false;
    }

    public boolean credit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        return true;
    }
}
