package com.revolut.moneytransfer;

import com.revolut.moneytransfer.api.AccountAPIController;
import com.revolut.moneytransfer.api.TransferAPIController;
import com.revolut.moneytransfer.service.AccountService;
import com.revolut.moneytransfer.service.TransferService;
import com.revolut.moneytransfer.storage.InMemoryStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.StaticHandler;

import static com.revolut.moneytransfer.APIConstants.OPEN_API_CONTRACT_URL;

interface APIConstants {
    String OPEN_API_CONTRACT_URL = "money-transfer-api.yaml";
}

public class APIServer extends AbstractVerticle {

    private TransferAPIController transferAPIController;
    private AccountAPIController accountAPIController;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new APIServer());
    }

    @Override
    public void start(final Future<Void> bootstrapFuture) throws Exception {

        OpenAPI3RouterFactory.create(vertx, OPEN_API_CONTRACT_URL, ar -> {

            if (ar.succeeded()) {

                initializeServices();

                OpenAPI3RouterFactory factory = ar.result();

                factory.addHandlerByOperationId("listAccounts", accountAPIController::listAccounts);
                factory.addHandlerByOperationId("getAccount", accountAPIController::getAccount);
                factory.addHandlerByOperationId("createAccount", accountAPIController::createAccount);
                factory.addHandlerByOperationId("deactivateAccount", accountAPIController::deactivateAccount);
                factory.addHandlerByOperationId("getAccountsTransfers", accountAPIController::listAccountsTransfers);

                factory.addHandlerByOperationId("listTransfers", transferAPIController::listTransfers);
                factory.addHandlerByOperationId("getTransfer", transferAPIController::getTransfer);
                factory.addHandlerByOperationId("createTransfer", transferAPIController::createTransfer);
                factory.addHandlerByOperationId("executeTransfer", transferAPIController::executeTransfer);
                factory.addHandlerByOperationId("cancelTransfer", transferAPIController::cancelTransfer);

                Router router = factory.getRouter();
                router.route("/*").handler(StaticHandler.create());
                vertx.createHttpServer(new HttpServerOptions()
                        .setPort(8080))
                        .requestHandler(router::accept)
                        .listen();

                bootstrapFuture.complete();
            } else {
                bootstrapFuture.fail(ar.cause());
            }
        });
    }

    private void initializeServices() {
        InMemoryStorage storage = new InMemoryStorage();
        storage.dummyData();
        AccountService accountService = new AccountService(storage);
        TransferService transferService = new TransferService(storage, accountService);
        accountAPIController = new AccountAPIController(accountService, transferService);
        transferAPIController = new TransferAPIController(accountService, transferService);
    }

}