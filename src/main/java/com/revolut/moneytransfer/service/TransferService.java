package com.revolut.moneytransfer.service;

import com.revolut.moneytransfer.model.Account;
import com.revolut.moneytransfer.model.CreateTransferRequest;
import com.revolut.moneytransfer.model.Transfer;
import com.revolut.moneytransfer.model.Transfers;
import com.revolut.moneytransfer.service.error.BusinessException;

import java.util.Optional;

public class TransferService {
    private IDGenerator idGenerator = new IDGenerator();
    private Transfers transferList = new Transfers();
    private AccountService accountService = new AccountService();

    public Transfers listTransfers() {
        return transferList;
    }

    public Transfer createTransfer(CreateTransferRequest request) {
        Optional<Account> fromAccount = accountService.getAccount(request.getFromAccountId());
        Optional<Account> toAccount = accountService.getAccount(request.getToAccountId());
        if (!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new BusinessException("Account not found exception");
        }
        if (accountService.debit(fromAccount.get(), request.getAmount()) && accountService.credit(toAccount.get(), request.getAmount())) {
            Transfer transfer = new Transfer(idGenerator.generateTransferId(), request.getFromAccountId(), request.getToAccountId(), request.getAmount(), request.getCurrency(), "PENDING");
            transferList.add(transfer);
            return transfer;
        }

        throw new BusinessException("Impossible to create a transfer");
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
}
