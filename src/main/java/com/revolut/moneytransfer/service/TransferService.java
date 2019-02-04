package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.entity.AccountEntity;
import com.revolut.moneytransfer.entity.TransferEntity;
import com.revolut.moneytransfer.service.error.BusinessException;
import com.revolut.moneytransfer.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.revolut.moneytransfer.entity.TransferEntity.State.PENDING;
import static com.revolut.moneytransfer.storage.Storage.EntityName.TRANSFER;

public class TransferService {
    private IDGenerator idGenerator = new IDGenerator();
    private Storage storage;
    private AccountService accountService;

    public TransferService(Storage storage, AccountService accountService) {
        this.storage = storage;
        this.accountService = accountService;
    }

    public List<TransferEntity> listTransfers() {
        return storage.getAll(TRANSFER);
    }

    public TransferEntity createTransfer(CreateTransferRequest request) {
        Optional<AccountEntity> fromAccount = accountService.getAccount(request.getFromAccountId());
        Optional<AccountEntity> toAccount = accountService.getAccount(request.getToAccountId());
        if (!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new BusinessException("Account not found");
        }
        return storage.save(TRANSFER, new TransferEntity(idGenerator.generateTransferId(), request.getFromAccountId(), request.getToAccountId(), request.getAmount(), request.getCurrency(), PENDING));
    }

    public Optional<TransferEntity> getTransfer(String transferId) {
        return storage.getById(TRANSFER, transferId);
    }

    public TransferEntity executeTransfer(String transferId) {
        Optional<TransferEntity> transfer = getTransfer(transferId);
        if (!transfer.isPresent()) {
            throw new BusinessException("Account not found");
        }
        Optional<AccountEntity> fromAccount = accountService.getAccount(transfer.get().getFromAccountId());
        Optional<AccountEntity> toAccount = accountService.getAccount(transfer.get().getToAccountId());
        if (!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new BusinessException("Account not found exception");
        }
        try {
            AccountEntity debitedAccount = fromAccount.get().debit(transfer.get().getAmount());
            AccountEntity creditedAccount = toAccount.get().credit(transfer.get().getAmount());
            accountService.save(debitedAccount);
            accountService.save(creditedAccount);
            return storage.save(TRANSFER, transfer.get().executed());
        } catch (RuntimeException exception) {
            //Rollback transaction and set transfer to failed
            accountService.save(fromAccount.get());
            accountService.save(toAccount.get());
            storage.save(TRANSFER, transfer.get().failed());
            throw new BusinessException("Impossible to transfer money between accounts", exception);
        }
    }

    public Optional<TransferEntity> cancelTransfer(String transferId) {
        Optional<TransferEntity> transfer = getTransfer(transferId);
        if (transfer.isPresent()) {
            return Optional.of(transfer.get().canceled());
        }
        return Optional.empty();
    }

    public List<TransferEntity> listTransfersByAccount(String accountId) {
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (account.isPresent()) {
            return storage.<TransferEntity>getAll(TRANSFER)
                    .stream()
                    .filter(transfer -> account.get().matchId(transfer.getFromAccountId())
                            || account.get().matchId(transfer.getToAccountId())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
