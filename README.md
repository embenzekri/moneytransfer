# moneytransfer

Lightweight and simple REST API for money transfer between accounts

Contents
--------
- [Features](#features)
- [Technologies](#Technologies)
- [Building and running application](#building-and-running-the-application)
- [REST API](#rest-api)
- [Architecture](#Architecture)
- [Tests](#tests)
- [Code Quality](#code-quality)
- [Missing features](#Missing-features)

Features
--------

- OpenAPI contract (v3) with swagger-ui documentation
- Account create/deactivate/listTransfers
- Transfer create/execute/cancel
- Meaningful HTTP return codes
- In-memory data storage
- Stand-alone jar with embedded web server
- Integration tests (using REST-assured)



Technologies
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

REST API
---------
The endpoints can be viewed and tested using the swagger-ui, available at [http://localhost:8080](http://localhost:8080/).

The OpenAPI v3 specification file is located in `src/resources/money-transfer-api.yaml`

Here are some key considerations of the API:
- HTTP status codes, HTTP methods and content types are used
- The id of the resources contains the name of the resource like `/accounts/123`, this enforces the Uniform API approach and makes it easy to query the API without prior knowledge.
- The links array in the resources responses, are part of the HATEOAS constraints and aims to provide an easy navigation.

The API is intentionally missing some key features/operation for the aim of simplicity.


Architecture
------------
The project is inspired by the Clean Architecture, where:
 - the business package contains the Core Business Logic.
 - the api + storage packages represents the Interfaces / Adapters containing the data storage and the REST API.

The implementation is thread safe, thanks to Vert.x concurrency model, there is one verticle, the APIServer which will always run in the same thread.

The business entities are also made immutable to reinforce consistency and thread safety.

**One important note: This is not 100% correctly implemented for the aim of simplicity.**

The project uses Vert.x core and web framework capabilities only to handle the web server creation and requests routing.

The vertx-web-api-contract allows to use OpenApi 3 specification directly inside the code using the design first approach and provides nice features like request validation and automatic 501 response for not implemented operations.

          
Tests
-----

The **integration tests** are writen using JUnit and REST Assured, and can be executed using:

```
mvn test
```

Jacoco generated test report can be found in `target/site/jacoco/`

Code quality
----------

The project is using the Intellij's default code styling.
The code follows SOLID and GRASP principles.

Missing features
-------

Security / Authentication
REST API Maturity level 4
Other API operations like DELETE / UPDATE / QUERY
Reactive programming and non blocking (Vert.x's main )
Enforced model and use cases validation 
Continuous integration (Jenkins, Sonar..)
...