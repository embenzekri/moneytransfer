import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.revolut.moneytransfer.APIServer;
import com.revolut.moneytransfer.api.schemas.Account;
import com.revolut.moneytransfer.api.schemas.CreateTransferRequest;
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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class APITest {
    public static final String ACCOUNT1_ID = "/accounts/3fc6b414-cdb8-4b8f-beb5-fb08c2902f87";
    public static final String ACCOUNT2_ID = "/accounts/9aecab5d-3827-4624-97a9-11b1207c7a12";
    public static final String ACCOUNT3_ID = "/accounts/e9ccb93b-bded-41a3-8e7e-95c3a322a8ee";

    public static final String TRANSFER1_ID = "/transfers/467c34aa-cef8-bdef-8e7e-1er08c2901e90";
    public static final String TRANSFER2_ID = "/transfers/816c2a88-7205-4a3b-905b-048af106847d";
    private static int PORT = 8081;

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
    public void testListAccounts() {
        JsonPath jsonPath = get("/accounts").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getList("").size(), is(3));
        assertThat(jsonPath.getString("find { it.name=='account1' }.id"), is(ACCOUNT1_ID));
        assertThat(jsonPath.getString("find { it.name=='account2' }.id"), is(ACCOUNT2_ID));
    }

    @Test
    public void testGetAccount() {
        JsonPath jsonPath = get(ACCOUNT1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("name"), equalTo("account1"));
        assertThat(jsonPath.getFloat("balance"), equalTo(9000f));
        assertThat(jsonPath.getString("currency"), equalTo("EUR"));
    }

    @Test
    public void testGetAccountNotFound() {
        get("/accounts/123").then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testDeactivateAccount() {
        JsonPath jsonPath = get(ACCOUNT1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("state"), equalTo("ACTIVE"));

        jsonPath = get(ACCOUNT1_ID + "/deactivate").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getString("id"), notNullValue());
        assertThat(jsonPath.getString("state"), equalTo("INACTIVE"));
    }

    @Test
    public void testListAccountsTransfers() {
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
    public void testCreateAccount() {
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
    public void testCreateTransfer() {
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
    public void testGetTransferNotFound() {
        get("/transfers/123").then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void testGetAndExecuteTransfer() {
        JsonPath getJsonPath = get(TRANSFER1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(getJsonPath.getString("id"), equalTo(TRANSFER1_ID));
        assertThat(getJsonPath.getString("state"), equalTo("PENDING"));

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
        JsonPath executeJsonPath = get(executeTransferUrl).then()
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
    public void testGetAndCancelTransfer() {
        JsonPath getJsonPath = get(TRANSFER1_ID).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(getJsonPath.getString("id"), equalTo(TRANSFER1_ID));
        assertThat(getJsonPath.getString("state"), equalTo("PENDING"));

        String cancelTransferUrl = getJsonPath.getString("links.find { it.rel=='cancel' }.href");

        JsonPath executeJsonPath = get(cancelTransferUrl).then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(executeJsonPath.getString("id"), equalTo(TRANSFER1_ID));
        assertThat(executeJsonPath.getString("state"), equalTo("CANCELED"));

    }

    private Account getAccount(String accountName, int balance, String currency) {
        return new Account(null, accountName, new BigDecimal(balance), currency, null, null);
    }

}
