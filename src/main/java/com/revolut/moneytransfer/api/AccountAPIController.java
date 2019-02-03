package com.revolut.moneytransfer.api;

import com.revolut.moneytransfer.api.schemas.*;
import com.revolut.moneytransfer.api.schemas.Error;
import com.revolut.moneytransfer.entity.AccountEntity;
import com.revolut.moneytransfer.entity.TransferEntity;
import com.revolut.moneytransfer.service.AccountService;
import com.revolut.moneytransfer.service.TransferService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.Optional;
import java.util.stream.Collectors;

public class AccountAPIController {
    private AccountService accountService;
    private TransferService transferService;

    public AccountAPIController(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    public void listAccounts(RoutingContext routingContext) {
        successfulResponse(routingContext, accountService.listAccounts().stream().map(accountEntity -> this.convertAccount(accountEntity)).collect(Collectors.toList()));
    }

    public void getAccount(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (!account.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, account.get());
        }
    }

    public void deactivateAccount(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.deactivateAccount(accountId);
        if (!account.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, account.get());
        }
    }

    public void createAccount(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateAccountRequest createAccountRequest = params.body().getJsonObject().mapTo(CreateAccountRequest.class);

        successfulResponse(routingContext, convertAccount(accountService.createAccount(createAccountRequest)));
    }

    public void listAccountsTransfers(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<AccountEntity> account = accountService.getAccount(accountId);
        if (!account.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, transferService.listTransfersByAccount(accountId));
        }
    }


    private void successfulResponse(RoutingContext routingContext, Object jsonObject) {
        routingContext
                .response()
                .setStatusCode(200)
                .end(Json.encodePrettily(jsonObject));
    }

    public Account convertAccount(AccountEntity accountEntity) {
        return new Account(accountEntity.getId(), accountEntity.getName(), accountEntity.getBalance(), accountEntity.getCurrency(), accountEntity.getState().name());
    }

}
