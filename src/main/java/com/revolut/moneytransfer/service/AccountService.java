package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.entity.AccountEntity;
import com.revolut.moneytransfer.api.schemas.CreateAccountRequest;
import com.revolut.moneytransfer.storage.Storage;

import java.util.List;
import java.util.Optional;

import static com.revolut.moneytransfer.entity.AccountEntity.State.ACTIVE;
import static com.revolut.moneytransfer.storage.Storage.EntityName.ACCOUNT;

public class AccountService {
    private IDGenerator idGenerator = new IDGenerator();
    private Storage storage;

    public AccountService(Storage storage) {
        this.storage = storage;
    }

    public List<AccountEntity> listAccounts() {
        return storage.getAll(ACCOUNT);
    }

    public AccountEntity createAccount(CreateAccountRequest request) {
        AccountEntity accountEntity = new AccountEntity(idGenerator.generateAccountId(), request.getName(), request.getBalance(), request.getCurrency(), ACTIVE);
        return storage.save(ACCOUNT, accountEntity);
    }

    public Optional<AccountEntity> getAccount(String accountId) {
        return storage.getById(ACCOUNT, accountId);
    }

    public Optional<AccountEntity> deactivateAccount(String accountId) {
        Optional<AccountEntity> account = getAccount(accountId);
        if (account.isPresent()) {
            AccountEntity deactivatedAccount = account.get().deactivate();
            storage.save(ACCOUNT, deactivatedAccount);
            return Optional.of(deactivatedAccount);
        }
        return Optional.empty();
    }

    public AccountEntity save(AccountEntity creditedAccount) {
        return storage.save(ACCOUNT, creditedAccount);
    }
}

