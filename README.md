# Xcepto for Java (xceptoj)

[![Xcepto Java release pipeline](https://github.com/xcepto/xceptoj/actions/workflows/cd.yaml/badge.svg)](https://github.com/xcepto/xceptoj/actions/workflows/cd.yaml)

Xcepto is a BDD testing framework for distributed systems.
Tests are specified **declaratively** here.

## Getting Started

Test specification happens according to the [Given-When-Then](https://dannorth.net/blog/introducing-bdd/#atm-example) pattern.

### Mental model

Test steps are **not immediately executed** when called!
They are compiled into a **state machine**,
where **transition** depends on the specified **conditions**.

The states are linked in a chain:

`[Start] -> [First] -> [Second] -> [Third] -> [Final]`

The test passes, if the `Final` state was reached before a **timeout**.

### Given
`Xcepto.given` introduces a specification environment based on a scenario.
```java
Xcepto.given(scenario, builder ->
{
    // Declare test behaviour here
}
```

`Scenario` classes specify instructions to setup and prepare the system under test.

### When (Actions)
Actions often require interfacing technologies.
Official adapters can be used to integrate some popular ones.

```java
Xcepto.given(scenario, builder ->
{
    var rest = builder.registerAdapter(new RestXceptoAdapter());

    // When
    rest.postRequest("localhost:3000", new SomeRequest(), SomeResponse.class, 
                     response -> response.value > 1000);
}
```

Post requests in particular also enable response validation (so they are hybrid action/expectation).

### Then (Expectations)

Expectations represent transition conditions.

RabbitMQ can be used to block transition until a certain kind of message is published.

```java
Xcepto.given(scenario, builder ->
{
    var rabbitMq = builder.registerAdapter(new RabbitMqXceptoAdapter(config));
    
    // When some Action happens
    
    // Then expect a certain response
    rabbitMq.eventCondition(ResponseMessage.class, 
                            e -> e.someValue == 1234);
}
```

### Full example

Inbound flow in a warehouse scenario:
Expect that the backend message bus publishes `StockReplenishedEvent`
whenever the client initiates a POST request to `http://localhost:3000/shipment/accept`.

```java
@Test
public void smallShipmentReplenishesStock() throws XceptoScenarioResetException, XceptoAdapterInitializationException, XceptoTestFailedException, XceptoAdapterTerminationException {

  InboundFlowScenario scenario = new InboundFlowScenario();

  // Given
  Xcepto.given(scenario, builder -> {
    RabbitMqXceptoAdapter rabbitmq = builder.registerAdapter(
        new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(
            scenario.getPort("rabbitmq", 5672))));
    RestXceptoAdapter rest = builder.registerAdapter(new RestXceptoAdapter());

    // Arrange
    URI acceptUrl = URI.create("http://localhost:%d/shipment/accept".formatted(
        scenario.getPort("examples.warehouse.inbound", 8081)));
    AcceptShipmentRequest request = new AcceptShipmentRequest();
    request.amount = 50;

    // When
    rest.postRequest(acceptUrl, request, AcceptShipmentResponse.class, response -> response.amount == request.amount);

    // Then
    rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total == request.amount);
  }, Duration.ofSeconds(30), Duration.ofMillis(100));
}
```

Here, `InboundFlowScenario` starts the production environment using **docker compose**.
The message bus also has to be configured.
The configuration passed to the RabbitMQ adapter describes the exchanges, 
queues and keys and references the docker container as a host.

## Adapters

Xcepto supports several technologies through adapters.

**Core** library:
- org.xcepto:xceptoj

Adapters:
- org.xcepto:xceptoj-rabbitmq (listening for messages)
- org.xcepto:xceptoj-rest (sending POST requests, validating POST responses)
