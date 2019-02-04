package com.revolut.moneytransfer;

import com.revolut.moneytransfer.api.AccountAPIController;
import com.revolut.moneytransfer.api.TransferAPIController;
import com.revolut.moneytransfer.api.schemas.Error;
import com.revolut.moneytransfer.business.service.AccountService;
import com.revolut.moneytransfer.business.service.TransferService;
import com.revolut.moneytransfer.business.service.error.BusinessException;
import com.revolut.moneytransfer.business.service.error.BusinessFailure;
import com.revolut.moneytransfer.storage.InMemoryStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;

import java.util.function.Function;

import static com.revolut.moneytransfer.business.service.error.BusinessFailure.ACCOUNT_NOT_FOUND;
import static com.revolut.moneytransfer.business.service.error.BusinessFailure.TRANSFER_NOT_FOUND;

public class APIServer extends AbstractVerticle {
    private final static String OPEN_API_CONTRACT_URL = "money-transfer-api.yaml";

    private TransferAPIController transferAPIController;
    private AccountAPIController accountAPIController;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new APIServer());
    }

    @Override
    public void start(final Future<Void> bootstrapFuture) throws Exception {

        OpenAPI3RouterFactory.create(
                vertx,
                OPEN_API_CONTRACT_URL,
                ar -> {
                    if (ar.succeeded()) {

                        initializeServices();

                        OpenAPI3RouterFactory factory = ar.result();

                        factory.addHandlerByOperationId("listAccounts", accountAPIController::listAccounts);
                        factory.addHandlerByOperationId("getAccount", accountAPIController::getAccount);
                        factory.addHandlerByOperationId("createAccount", accountAPIController::createAccount);
                        factory.addHandlerByOperationId(
                                "deactivateAccount", accountAPIController::deactivateAccount);
                        factory.addHandlerByOperationId(
                                "getAccountsTransfers", accountAPIController::listAccountsTransfers);

                        factory.addHandlerByOperationId("listTransfers", transferAPIController::listTransfers);
                        factory.addHandlerByOperationId("getTransfer", transferAPIController::getTransfer);
                        factory.addHandlerByOperationId("createTransfer", transferAPIController::createTransfer);
                        //factory.addHandlerByOperationId("executeTransfer", transferAPIController::executeTransfer);
                        factory.addHandlerByOperationId("cancelTransfer", transferAPIController::cancelTransfer);

                        factory.addHandlerByOperationId("executeTransfer", new APIHandler(transferAPIController::executeTransfer));

                        Router router = factory.getRouter();
                        //configureRouter(router);
                        vertx
                                .createHttpServer(new HttpServerOptions().setPort(8080))
                                .requestHandler(router::handle)
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
        transferAPIController = new TransferAPIController(transferService);
    }

    /*private void configureRouter(Router router) {
        router.route().handler(
                CorsHandler.create(".+")
                        .maxAgeSeconds(600)
                        .allowedMethod(HttpMethod.GET)
                        .allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.PUT)
                        .allowedMethod(HttpMethod.DELETE)
                        .allowedMethod(HttpMethod.OPTIONS)
                        .allowedHeader("Content-Type")
                        .allowedHeader("Accept")
                        .allowedHeader("Accept-Language")
                        .allowedHeader("Authorization"));
        router.route("/*").handler(StaticHandler.create().setCachingEnabled(false));
    }*/
}

class APIHandler implements Handler<RoutingContext> {
    private Function<RoutingContext, Object> delegate;

    public APIHandler(Function<RoutingContext, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        try {
            Object result = delegate.apply(routingContext);
            response
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(result));
        } catch (BusinessException ex) {
            BusinessFailure businessFailure = ex.getBusinessFailure();
            if (ACCOUNT_NOT_FOUND == businessFailure || TRANSFER_NOT_FOUND == businessFailure) {
                response.setStatusCode(404);
            } else {
                response.setStatusCode(400);
            }
            response.end(Json.encodePrettily(new Error(businessFailure.name(), businessFailure.getMessage())));
        }
    }
}