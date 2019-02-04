package com.revolut.moneytransfer.business.service;

import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.business.entity.AccountEntity;
import com.revolut.moneytransfer.business.entity.TransferEntity;
import com.revolut.moneytransfer.business.service.error.BusinessException;
import com.revolut.moneytransfer.business.service.error.TechnicalException;
import com.revolut.moneytransfer.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.revolut.moneytransfer.business.service.error.BusinessFailure.*;
import static com.revolut.moneytransfer.storage.Storage.EntityName.TRANSFER;

public class TransferService {
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
            throw new BusinessException(ACCOUNT_NOT_FOUND);
        }
        return storage.save(TRANSFER, new TransferEntity(request.getFromAccountId(), request.getToAccountId(), request.getAmount(), request.getCurrency()));
    }

    public Optional<TransferEntity> getTransfer(String transferId) {
        return storage.getById(TRANSFER, transferId);
    }

    public TransferEntity executeTransfer(String transferId) {
        Optional<TransferEntity> transfer = getTransfer(transferId);
        if (!transfer.isPresent()) {
            throw new BusinessException(ACCOUNT_NOT_FOUND);
        }
        if (!transfer.get().isPending()) {
            throw new BusinessException(TRANSFER_STATE_INVALID);
        }
        Optional<AccountEntity> fromAccount = accountService.getAccount(transfer.get().getFromAccountId());
        Optional<AccountEntity> toAccount = accountService.getAccount(transfer.get().getToAccountId());
        if (!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new BusinessException(ACCOUNT_NOT_FOUND);
        }
        if (!fromAccount.get().isActive() || !toAccount.get().isActive()) {
            throw new BusinessException(ACCOUNT_STATE_INVALID);
        }
        if (fromAccount.get().getBalance().subtract(transfer.get().getAmount()).intValue() < 0) {
            throw new BusinessException(TRANSFER_AMOUNT_EXCEEDED);
        }
        try {
            return doExecuteTransfer(transfer.get(), fromAccount.get(), toAccount.get());
        } catch (RuntimeException exception) {
            rollbackTransfer(transfer.get(), fromAccount.get(), toAccount.get());
            throw new TechnicalException("Impossible to transfer money between accounts", exception);
        }
    }

    private void rollbackTransfer(TransferEntity transfer, AccountEntity fromAccount, AccountEntity toAccount) {
        accountService.save(fromAccount);
        accountService.save(toAccount);
        storage.save(TRANSFER, transfer.failed());
    }

    private TransferEntity doExecuteTransfer(TransferEntity transfer, AccountEntity fromAccount, AccountEntity toAccount) {
        AccountEntity debitedAccount = fromAccount.debit(transfer.getAmount());
        AccountEntity creditedAccount = toAccount.credit(transfer.getAmount());
        accountService.save(debitedAccount);
        accountService.save(creditedAccount);
        return storage.save(TRANSFER, transfer.executed());
    }

    public Optional<TransferEntity> cancelTransfer(String transferId) {
        Optional<TransferEntity> transfer = getTransfer(transferId);
        if (transfer.isPresent()) {
            TransferEntity canceledTransfer = transfer.get().canceled();
            return Optional.of(storage.save(TRANSFER, canceledTransfer));
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
