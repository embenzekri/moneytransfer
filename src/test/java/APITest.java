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

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class APITest {
    public static final String ACCOUNT1_ID = "/accounts/3fc6b414-cdb8-4b8f-beb5-fb08c2902f87";
    public static final String ACCOUNT2_ID = "/accounts/9aecab5d-3827-4624-97a9-11b1207c7a12";
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
        assertThat(jsonPath.getFloat("balance"), equalTo(1000f));
        assertThat(jsonPath.getString("currency"), equalTo("EUR"));
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
        JsonPath jsonPath = get(ACCOUNT1_ID+"/transfers").then()
                .assertThat()
                .statusCode(200).extract().jsonPath();

        assertThat(jsonPath.getList("").size(), is(3));
        assertThat(jsonPath.getString("find { it.name=='account1' }.id"), is(ACCOUNT1_ID));
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
        CreateTransferRequest createTransferRequest = new CreateTransferRequest(ACCOUNT1_ID, ACCOUNT2_ID, new BigDecimal(1000), "EUR");

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

    private Account getAccount(String accountName, int balance, String currency) {
        return new Account(null, accountName, new BigDecimal(balance), currency, null);
    }

}
