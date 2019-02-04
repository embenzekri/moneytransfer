import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.revolut.moneytransfer.APIServer;
import com.revolut.moneytransfer.api.schemas.Account;
import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
import com.revolut.moneytransfer.business.service.error.BusinessFailure;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.post;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class APITest {
    public static final String ACCOUNT1_ID = "/accounts/3fc6b414-cdb8-4b8f-beb5-fb08c2902f87";
    public static final String ACCOUNT2_ID = "/accounts/9aecab5d-3827-4624-97a9-11b1207c7a12";
    public static final String ACCOUNT3_ID = "/accounts/e9ccb93b-bded-41a3-8e7e-95c3a322a8ee";

    public static final String TRANSFER1_ID = "/transfers/467c34aa-cef8-bdef-8e7e-1er08c2901e90";
    public static final String TRANSFER2_ID = "/transfers/816c2a88-7205-4a3b-905b-048af106847d";
    private static int PORT = 8080;

    private Vertx vertx;

    @BeforeClass
    public static void initialize() throws IOException {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = PORT;
    }

    @Before
    public void setup(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        vertx.deployVerticle(APIServer.class.getName(), new DeploymentOptions(), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown() {
        RestAssured.reset();
    }

    @Test
    public void shouldListAccounts() {
        JsonPath jsonPath = get("/accounts").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getList("").size(), is(3));
        assertThat(jsonPath.getString("find { it.name=='account1' }.id"), is(ACCOUNT1_ID));
        assertThat(jsonPath.getString("find { it.name=='account2' }.id"), is(ACCOUNT2_ID));
    }

    @Test
    public void shouldGetAccount() {
        JsonPath jsonPath = get(ACCOUNT1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("name"), equalTo("account1"));
        assertThat(jsonPath.getFloat("balance"), equalTo(9000f));
        assertThat(jsonPath.getString("currency"), equalTo("EUR"));
    }

    @Test
    public void shouldGetAccountNotFound() {
        get("/accounts/123").then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void shouldDeactivateAccount() {
        JsonPath jsonPath = get(ACCOUNT1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("state"), equalTo("ACTIVE"));

        jsonPath = post(ACCOUNT1_ID + "/deactivate").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("state"), equalTo("INACTIVE"));
    }

    @Test
    public void shouldListAccountsTransfers() {
        JsonPath jsonPath = get(ACCOUNT1_ID + "/transfers").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getList("").size(), is(2));
        assertThat(jsonPath.getString("find { it.id=='" + TRANSFER1_ID + "' }.fromAccountId"), is(ACCOUNT1_ID));
        assertThat(jsonPath.getString("find { it.id=='" + TRANSFER1_ID + "' }.toAccountId"), is(ACCOUNT2_ID));

        assertThat(jsonPath.getString("find { it.id=='" + TRANSFER2_ID + "' }.fromAccountId"), is(ACCOUNT1_ID));
        assertThat(jsonPath.getString("find { it.id=='" + TRANSFER2_ID + "' }.toAccountId"), is(ACCOUNT3_ID));
    }

    @Test
    public void shouldCreateAccount() {
        RequestSpecification request = RestAssured.given();
        Account account = getAccount("Mehdi", 3000, "EUR");
        request.contentType("application/json");
        request.body(Json.encodePrettily(account));

        JsonPath jsonPath = request.post("/accounts").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("name"), equalTo(account.getName()));
        assertThat(jsonPath.getFloat("balance"), equalTo(account.getBalance().floatValue()));
        assertThat(jsonPath.getString("currency"), equalTo(account.getCurrency()));
    }

    @Test
    public void shouldCreateTransfer() {
        String requestId = UUID.randomUUID().toString();
        CreateTransferRequest createTransferRequest = new CreateTransferRequest(requestId, ACCOUNT1_ID, ACCOUNT2_ID, new BigDecimal(1000), "EUR");

        RequestSpecification request = RestAssured.given();

        request.contentType("application/json");
        request.body(Json.encodePrettily(createTransferRequest));

        JsonPath jsonPath = request.post("/transfers").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("fromAccountId"), equalTo(createTransferRequest.getFromAccountId()));
        assertThat(jsonPath.getString("toAccountId"), equalTo(createTransferRequest.getToAccountId()));
        assertThat(jsonPath.getFloat("amount"), equalTo(createTransferRequest.getAmount().floatValue()));
        assertThat(jsonPath.getString("currency"), equalTo(createTransferRequest.getCurrency()));
        assertThat(jsonPath.getString("state"), equalTo("PENDING"));
    }

    @Test
    public void shouldGetTransferNotFound() {
        get("/transfers/123").then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testCancelTransfer() {
        JsonPath getJsonPath = getPendingTransferJsonPath(TRANSFER1_ID);
        String cancelTransferUrl = getJsonPath.getString("links.find { it.rel=='cancel' }.href");

        JsonPath executeJsonPath = post(cancelTransferUrl).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(executeJsonPath.getString("id"), equalTo(TRANSFER1_ID));
        assertThat(executeJsonPath.getString("state"), equalTo("CANCELED"));

    }

    @Test
    public void shouldExecuteTransfer() {
        JsonPath getJsonPath = getPendingTransferJsonPath(TRANSFER1_ID);

        // Data before transfer execution
        String fromAccountId = getJsonPath.getString("fromAccountId");
        String toAccountId = getJsonPath.getString("toAccountId");
        float amount = getJsonPath.getFloat("amount");
        float fromAccountBalance = get(fromAccountId).then()
                .assertThat()
                .statusCode(200).extract().jsonPath().getFloat("balance");
        float toAccountBalance = get(toAccountId).then()
                .assertThat()
                .statusCode(200).extract().jsonPath().getFloat("balance");

        String executeTransferUrl = getJsonPath.getString("links.find { it.rel=='execute' }.href");

        // Execute transfer and check result
        JsonPath executeJsonPath = post(executeTransferUrl).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(executeJsonPath.getString("id"), equalTo(TRANSFER1_ID));
        assertThat(executeJsonPath.getString("state"), equalTo("COMPLETED"));

        assertThat(get(fromAccountId).then()
                .assertThat()
                .statusCode(200).extract().jsonPath().getFloat("balance"), equalTo(fromAccountBalance - amount));
        assertThat(get(fromAccountId).then()
                .assertThat()
                .statusCode(200).extract().jsonPath().getFloat("balance"), equalTo(toAccountBalance + amount));
    }

    @Test
    public void shouldTransferFailWhenDeactivatedUser() {
        // Get transfer in pending state
        JsonPath transferJsonPath = getPendingTransferJsonPath(TRANSFER1_ID);

        String fromAccountId = transferJsonPath.getString("fromAccountId");

        // Deactivate the source account
        JsonPath accountJsonPath = post(fromAccountId + "/deactivate").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(accountJsonPath.getString("state"), equalTo("INACTIVE"));

        String executeTransferUrl = transferJsonPath.getString("links.find { it.rel=='execute' }.href");

        // Execute transfer and check that transfer failed
        JsonPath executeJsonPath = post(executeTransferUrl).then()
                .assertThat()
                .statusCode(400).extract().jsonPath();

        assertBusinessError(executeJsonPath, BusinessFailure.ACCOUNT_STATE_INVALID);
    }

    @Test
    public void testTransferFailedWhenAmountExceeded() {
        // Get transfer in pending state
        JsonPath transferJsonPath = getPendingTransferJsonPath(TRANSFER2_ID);

        String executeTransferUrl = transferJsonPath.getString("links.find { it.rel=='execute' }.href");

        // Execute transfer and check that transfer failed
        JsonPath executeJsonPath = post(executeTransferUrl).then()
                .assertThat()
                .statusCode(400).extract().jsonPath();

        assertBusinessError(executeJsonPath, BusinessFailure.TRANSFER_AMOUNT_EXCEEDED);
    }

    private void assertBusinessError(JsonPath executeJsonPath, BusinessFailure accountStateInvalid2) {
        BusinessFailure accountStateInvalid = accountStateInvalid2;
        assertThat(executeJsonPath.getString("code"), equalTo(accountStateInvalid.name()));
        assertThat(executeJsonPath.getString("message"), equalTo(accountStateInvalid.getMessage()));
    }

    private JsonPath getPendingTransferJsonPath(String transfer1Id) {
        JsonPath getJsonPath = get(transfer1Id).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(getJsonPath.getString("id"), equalTo(transfer1Id));
        assertThat(getJsonPath.getString("state"), equalTo("PENDING"));
        return getJsonPath;
    }

    private Account getAccount(String accountName, int balance, String currency) {
        return new Account(null, accountName, new BigDecimal(balance), currency, null, null);
    }

}
