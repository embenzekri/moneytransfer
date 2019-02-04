package com.revolut.moneytransfer.api;

import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.api.schemas.Link;
import com.revolut.moneytransfer.api.schemas.Transfer;
import com.revolut.moneytransfer.business.entity.AccountEntity;
import com.revolut.moneytransfer.business.entity.TransferEntity;
import com.revolut.moneytransfer.business.service.TransferService;
import com.revolut.moneytransfer.business.service.error.BusinessException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.revolut.moneytransfer.business.service.error.BusinessFailure.TRANSFER_NOT_FOUND;

public class TransferAPIController {
    private TransferService transferService;

    public TransferAPIController(TransferService transferService) {
        this.transferService = transferService;
    }

    public List<Transfer> listTransfers(RoutingContext routingContext) {
        List<Transfer> transferList =
                transferService.listTransfers().stream()
                        .map(this::convertTransfer)
                        .collect(Collectors.toList());

        return transferList;
    }

    public Transfer createTransfer(RoutingContext routingContext) {
        RequestParameters params = routingContext.get("parsedParameters");

        CreateTransferRequest createTransferRequest =
                params.body().getJsonObject().mapTo(CreateTransferRequest.class);

        return convertTransfer(transferService.createTransfer(createTransferRequest));
    }

    public Transfer getTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            throw new BusinessException(TRANSFER_NOT_FOUND);
        }
        return convertTransfer(transfer.get());
    }

    public Transfer executeTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            throw new BusinessException(TRANSFER_NOT_FOUND);
        }
        TransferEntity executedTransfer = transferService.executeTransfer(transferId);
        return convertTransfer(executedTransfer);
    }

    public Transfer cancelTransfer(RoutingContext routingContext) {
        String transferId = routingContext.pathParam("id");
        Optional<TransferEntity> transfer = transferService.getTransfer(transferId);
        if (!transfer.isPresent()) {
            throw new BusinessException(TRANSFER_NOT_FOUND);
        }
        Optional<TransferEntity> executedTransfer = transferService.cancelTransfer(transferId);
        if (!executedTransfer.isPresent()) {
            throw new BusinessException(TRANSFER_NOT_FOUND);
        }
        return convertTransfer(executedTransfer.get());
    }

    public List<Transfer> listTransfersByAccount(AccountEntity accountEntity) {
        return transferService.listTransfers()
                .stream()
                .filter(transfer -> accountEntity.matchId(transfer.getFromAccountId())
                        || accountEntity.matchId(transfer.getToAccountId())).map(this::convertTransfer).collect(Collectors.toList());
    }

    public Transfer convertTransfer(TransferEntity transferEntity) {
        List<Link> links = new ArrayList<>();
        links.add(new Link(transferEntity.getId() + "/execute", "execute", "POST"));
        links.add(new Link(transferEntity.getId() + "/cancel", "cancel", "POST"));
        return new Transfer(
                transferEntity.getId(),
                transferEntity.getFromAccountId(),
                transferEntity.getToAccountId(),
                transferEntity.getAmount(),
                transferEntity.getCurrency(),
                transferEntity.getState().name(),
                links);
    }
}
