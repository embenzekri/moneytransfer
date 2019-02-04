# moneytransfer

Lightweight and simple REST API for money transfer between accounts

Contents
--------
- [Features](#features)
- [Dependencies](#dependencies)
- [Building and running application](#building-and-running-application)
- [Endpoints](#endpoints)
- [Architecture](#Architecture)
- [Tests](#tests)
- [Code Quality](#static-code-analysis)

Features
--------

- OpenAPI contract (v3) with swagger-ui documentation
- Account create/deactivate/listTransfers
- Transfer create/execute/cancel
- Meaningful HTTP return codes
- In-memory data storage
- Stand-alone jar with embedded web server
- Integration tests (using REST-assured)



Dependencies
----------

The project depends on the following technologies:

- **Main Dependencies**: Java 8, [Maven](https://maven.apache.org), [vertx-core](https://vertx.io/docs/vertx-core/java), [vertx-web](https://github.com/vert-x3/vertx-web), [vertx-web-contract](https://vertx.io/docs/vertx-web-api-contract)
- **Test Dependencies**: [JUnit](https://junit.org/), [REST Assured](https://github.com/rest-assured/rest-assured), [vertx-unit](https://github.com/vert-x3/vertx-unit)

Building and running the application
--------------------------------

To package the application (also executing tests):

```
mvn clean package
```

To start the application:

```
java -jar target/moneytransfer-1.0-SNAPSHOT-fat.jar 
```

Server will start running on port `8080`

Endpoints
---------
The endpoints can be viewed and tested using the swagger-ui, available at [http://localhost:8080/].
The OpenAPI v3 specification file is located in src/resources/money-transfer-api.yaml

Architecture
------------
The project is inspired by the Clean Architecture, where:
 - the business package contains the Core Business Logic.
 - the api + storage packages represents the Interfaces / Adapters containing the data storage and the REST API.

**One important note: This is not 100% correctly implemented for the aim of simplicity.**

The project uses Vert.x core and web framework capabilities only to handle the web server creation and requests routing.

The vertx-web-api-contract allows to use OpenApi 3 specification directly inside the code using the design first approach and provides nice features like request validation and automatic 501 response for not implemented operations.

          
Tests
-----

The REST Assured **integration tests** can be executed using:

```
mvn test
```

Jacoco generated test report can be found in `target/site/jacoco/`

Code quality
----------

The project is using the Intellij's default code styling.
