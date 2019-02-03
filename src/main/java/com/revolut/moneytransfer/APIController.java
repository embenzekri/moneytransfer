package com.revolut.moneytransfer;

import com.revolut.moneytransfer.model.*;
import com.revolut.moneytransfer.model.Error;
import com.revolut.moneytransfer.service.AccountService;
import com.revolut.moneytransfer.service.TransferService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.Optional;

public class APIController {
    private AccountService accountService = new AccountService();
    private TransferService transferService = new TransferService();

    public void listAccounts(RoutingContext routingContext) {
        successfulResponse(routingContext, accountService.listAccounts());
    }

    public void getAccount(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<Account> account = accountService.getAccount(accountId);
        if (!account.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, account);
        }
    }

    public void createAccount(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateAccountRequest createAccountRequest = params.body().getJsonObject().mapTo(CreateAccountRequest.class);

        successfulResponse(routingContext, accountService.createAccount(createAccountRequest));
    }

    public void listTransfers(RoutingContext routingContext) {
        successfulResponse(routingContext, transferService.listTransfers());
    }

    public void createTransfer(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateTransferRequest createTransferRequest = params.body().getJsonObject().mapTo(CreateTransferRequest.class);

        Error error = new Error("BUSINESS", "");

        successfulResponse(routingContext, transferService.createTransfer(createTransferRequest));
    }

    public void getTransfer(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<Transfer> transfer = transferService.getTransfer(accountId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, transfer);
        }
    }

    public void executeTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<Transfer> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            Transfer executedTransfer = transferService.executeTransfer(transferId);
            successfulResponse(routingContext, executedTransfer);
        }
    }

    public void cancelTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<Transfer> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            Transfer executedTransfer = transferService.cancelTransfer(transferId);
            successfulResponse(routingContext, executedTransfer);
        }
    }

    private void successfulResponse(RoutingContext routingContext, Object jsonObject) {
        routingContext
                .response()
                .setStatusCode(200)
                .end(Json.encodePrettily(jsonObject));
    }
}
