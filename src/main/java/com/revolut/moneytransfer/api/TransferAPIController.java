package com.revolut.moneytransfer.api;

import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.api.schemas.Link;
import com.revolut.moneytransfer.api.schemas.Transfer;
import com.revolut.moneytransfer.entity.TransferEntity;
import com.revolut.moneytransfer.service.AccountService;
import com.revolut.moneytransfer.service.TransferService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TransferAPIController {
    private AccountService accountService;
    private TransferService transferService;

    public TransferAPIController(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    public void listTransfers(RoutingContext routingContext) {
        List<Transfer> transferList = transferService.listTransfers().stream().map(this::convertTransfer).collect(Collectors.toList());

        successfulResponse(routingContext, transferList);
    }

    public void createTransfer(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateTransferRequest createTransferRequest = params.body().getJsonObject().mapTo(CreateTransferRequest.class);

        successfulResponse(routingContext, convertTransfer(transferService.createTransfer(createTransferRequest)));
    }

    public void getTransfer(RoutingContext routingContext) {
        String accountId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(accountId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            successfulResponse(routingContext, convertTransfer(transfer.get()));
        }
    }

    public void executeTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            TransferEntity executedTransfer = transferService.executeTransfer(transferId);
            successfulResponse(routingContext, executedTransfer);
        }
    }

    public void cancelTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            routingContext
                    .response()
                    .setStatusCode(404)
                    .end();
        } else {
            Optional<TransferEntity> executedTransfer = transferService.cancelTransfer(transferId);
            successfulResponse(routingContext, convertTransfer(executedTransfer.get()));
        }
    }

    private void successfulResponse(RoutingContext routingContext, Object jsonObject) {
        routingContext
                .response()
                .setStatusCode(200)
                .end(Json.encodePrettily(jsonObject));
    }

    public Transfer convertTransfer(TransferEntity transferEntity) {
        List<Link> links = new ArrayList<>();
        links.add(new Link(transferEntity.getId() + "/execute", "execute", "POST"));
        links.add(new Link(transferEntity.getId() + "/cancel", "cancel", "POST"));
        return new Transfer(transferEntity.getId(), transferEntity.getFromAccountId(), transferEntity.getToAccountId(), transferEntity.getAmount(), transferEntity.getCurrency(), transferEntity.getState().name(), links);
    }
}
