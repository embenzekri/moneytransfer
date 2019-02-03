package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.entity.AccountEntity;
import com.revolut.moneytransfer.entity.TransferEntity;
import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.api.schemas.Transfer;
import com.revolut.moneytransfer.api.schemas.Transfers;
import com.revolut.moneytransfer.service.error.BusinessException;
import com.revolut.moneytransfer.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.revolut.moneytransfer.storage.Storage.EntityName.TRANSFER;

public class TransferService {
    private IDGenerator idGenerator = new IDGenerator();
    private Transfers transferList = new Transfers();
    private Storage storage;
    private AccountService accountService;

    public TransferService(Storage storage, AccountService accountService) {
        this.storage = storage;
        this.accountService = accountService;
    }

    public List<TransferEntity> listTransfers() {
        return storage.getAll(TRANSFER);
    }

    public Transfer createTransfer(CreateTransferRequest request) {
        Optional<AccountEntity> fromAccount = accountService.getAccount(request.getFromAccountId());
        Optional<AccountEntity> toAccount = accountService.getAccount(request.getToAccountId());
        if (!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new BusinessException("Account not found exception");
        }
        try {
            AccountEntity debitedAccount = fromAccount.get().debit(request.getAmount());
            AccountEntity creditedAccount = toAccount.get().credit(request.getAmount());
            accountService.save(debitedAccount);
            accountService.save(creditedAccount);
            Transfer transfer = new Transfer(idGenerator.generateTransferId(), request.getFromAccountId(), request.getToAccountId(), request.getAmount(), request.getCurrency(), "PENDING");
            transferList.add(transfer);
            return transfer;
        } catch (Exception exception) {
            throw new BusinessException("Impossible to transfer money between accounts");
        }
    }

    public Optional<Transfer> getTransfer(String transferId) {
        for (Transfer transfer : transferList) {
            if (transfer.getId().equals(transferId)) {
                return Optional.of(transfer);
            }
        }
        return Optional.empty();
    }

    public Transfer executeTransfer(String transferId) {
        Optional<Transfer> transfer = getTransfer(transferId);
        transfer.get().setState("COMPLETED");
        return transfer.get();
    }

    public Transfer cancelTransfer(String transferId) {
        Optional<Transfer> transfer = getTransfer(transferId);
        transfer.get().setState("CANCELED");
        return transfer.get();
    }

    public List<Transfer> listTransfersByAccount(String accountId) {
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (account.isPresent()) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
