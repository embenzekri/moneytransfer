package com.revolut.moneytransfer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

import static com.revolut.moneytransfer.APIConstants.OPEN_API_CONTRACT_URL;

interface APIConstants {
    String OPEN_API_CONTRACT_URL = "money-transfer-api.yaml";
}

public class APIServer extends AbstractVerticle {

    private APIController apiController = new APIController();

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new APIServer());
    }


    @Override
    public void start(final Future<Void> bootstrapFuture) throws Exception {

        OpenAPI3RouterFactory.create(vertx, OPEN_API_CONTRACT_URL, ar -> {

            if (ar.succeeded()) {
                OpenAPI3RouterFactory factory = ar.result();

                factory.addHandlerByOperationId("listAccounts", apiController::listAccounts);
                factory.addHandlerByOperationId("getAccount", apiController::getAccount);
                factory.addHandlerByOperationId("createAccount", apiController::createAccount);

                factory.addHandlerByOperationId("listTransfers", apiController::listTransfers);
                factory.addHandlerByOperationId("executeTransfer", apiController::getTransfer);
                factory.addHandlerByOperationId("createTransfer", apiController::createTransfer);
                factory.addHandlerByOperationId("executeTransfer", apiController::executeTransfer);
                factory.addHandlerByOperationId("cancelTransfer", apiController::cancelTransfer);

                Router router = factory.getRouter();

                vertx.createHttpServer(new HttpServerOptions()
                        .setPort(8081))
                        .requestHandler(router::accept)
                        .listen();

                bootstrapFuture.complete();
            } else {
                bootstrapFuture.fail(ar.cause());
            }
        });
    }

}