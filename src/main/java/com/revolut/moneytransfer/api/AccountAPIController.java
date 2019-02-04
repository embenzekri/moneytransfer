package com.revolut.moneytransfer.api;

import com.revolut.moneytransfer.api.schemas.Account;
import com.revolut.moneytransfer.api.schemas.CreateAccountRequest;
import com.revolut.moneytransfer.api.schemas.Link;
import com.revolut.moneytransfer.api.schemas.Transfer;
import com.revolut.moneytransfer.business.entity.AccountEntity;
import com.revolut.moneytransfer.business.service.AccountService;
import com.revolut.moneytransfer.business.service.error.BusinessException;
import com.revolut.moneytransfer.business.service.error.BusinessFailure;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountAPIController {
    private AccountService accountService;
    private TransferAPIController transferAPIController;

    public AccountAPIController(AccountService accountService, TransferAPIController transferAPIController) {
        this.accountService = accountService;
        this.transferAPIController = transferAPIController;
    }

    public List<Account> listAccounts(RoutingContext routingContext) {
        return accountService.listAccounts().stream().map(this::convertAccount).collect(Collectors.toList());
    }

    public Account getAccount(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (!account.isPresent()) {
            throw new BusinessException(BusinessFailure.ACCOUNT_NOT_FOUND);
        } else {
            return convertAccount(account.get());
        }
    }

    public Account deactivateAccount(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.deactivateAccount(accountId);
        if (!account.isPresent()) {
            throw new BusinessException(BusinessFailure.ACCOUNT_NOT_FOUND);
        } else {
            return convertAccount(account.get());
        }
    }

    public Account createAccount(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateAccountRequest createAccountRequest = params.body().getJsonObject().mapTo(CreateAccountRequest.class);

        return convertAccount(accountService.createAccount(createAccountRequest));
    }

    public List<Transfer> listAccountsTransfers(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (!account.isPresent()) {
            throw new BusinessException(BusinessFailure.ACCOUNT_NOT_FOUND);
        }
        return transferAPIController.listTransfersByAccount(account.get());
    }

    public Account convertAccount(AccountEntity accountEntity) {
        List<Link> links = new ArrayList<>();
        links.add(new Link(accountEntity.getId() + "/deactivate", "deactivate", "POST"));
        links.add(new Link(accountEntity.getId() + "/transfers", "list-transfers", "GET"));
        return new Account(accountEntity.getId(), accountEntity.getName(), accountEntity.getBalance(), accountEntity.getCurrency(), accountEntity.getState().name(), links);
    }

}
