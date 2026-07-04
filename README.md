# Xcepto for Java (xceptoj)

[![Xcepto Java release pipeline](https://github.com/xcepto/xceptoj/actions/workflows/cd.yaml/badge.svg)](https://github.com/xcepto/xceptoj/actions/workflows/cd.yaml)
[![semantic-release: conventional-commits](https://img.shields.io/badge/semantic--release-conventional--commits-e10079?logo=semantic-release)](https://www.conventionalcommits.org/en/v1.0.0/)

Xcepto is a declarative **system-test** framework for distributed systems that **replaces** manual retries, sleeping and exception handling with a **state-machine**-based execution model, aligned with [Given-When-Then](https://dannorth.net/blog/introducing-bdd/#atm-example) semantics.

## Why Xcepto?

Traditional system tests rely on implicit timing assumptions that lead to non-deterministic results. Artificial workarounds include retries and gates.

This becomes especially problematic in distributed systems, where events are asynchronous and ordering is not guaranteed. Forcing determinism with ad-hoc workarounds pollutes behavior specification into an unmaintainable mess.

Xcepto eliminates these issues by executing tests as condition-driven state machines, removing the need for timing assumptions entirely — making it a natural fit for systems built on asynchronous messaging as well as synchronous request-response interactions.

## Mental model

Test steps are **not executed immediately** when called!
They are compiled into a **state machine**,
where **transition** depends on the specified **conditions**.

The states are linked in a chain:

```
[Start] -> [First] -> [Second] -> [Third] -> [Final]
```

The test passes if the `Final` state was reached before a **timeout**.

## Getting Started

Add the core and the adapters you need to your test dependencies:

```groovy
// Gradle
testImplementation 'org.xcepto:xceptoj:VERSION'
testImplementation 'org.xcepto:xceptoj-rest:VERSION'   // REST (JSON) adapter
testImplementation 'org.xcepto:xceptoj-ssr:VERSION'    // SSR adapter
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.xcepto</groupId>
    <artifactId>xceptoj</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

### Given

`Xcepto.given` introduces a specification environment based on a scenario.

```java
var scenario = new YourScenario();
Xcepto.given(scenario, builder -> {
    // Declare test behaviour here
});
```

`Scenario` classes specify instructions to set up and prepare the system under test.

## Adapters

Raw Xcepto is just a runtime and lifecycle manager for tests.
Adapters integrate technologies into the Xcepto ecosystem.

### REST adapter

The REST adapter targets **JSON APIs**. It supports all HTTP verbs, request body serialization, bearer token authentication, typed response deserialization, and `Promise<T>` for passing data between steps.

```java
Xcepto.given(scenario, builder -> {
    var rest = new RestAdapterBuilder(builder)
        .withBaseUrl(URI.create("http://localhost:8080"))
        .withSerializer(new GsonSerializer())
        .build();

    // POST: executes once, asserts immediately
    rest.post("/shipment/accept")
        .withRequestBody(() -> new AcceptShipmentRequest(50))
        .withResponseType(AcceptShipmentResponse.class)
        .assertThatResponse((AcceptShipmentResponse r) -> r.amount == 50);

    // GET: retried until assertion passes or timeout
    rest.get("/inventory/stock")
        .assertSuccess()
        .assertThatResponseContentString(body -> body.contains("\"replenished\":true"));
});
```

**Idempotent verbs** (GET, PUT, DELETE) are **retried** until their assertions pass.
**Non-idempotent verbs** (POST, PATCH) execute **once** and fail immediately on assertion failure.

#### Promise — passing data between steps

```java
Xcepto.given(scenario, builder -> {
    var rest = new RestAdapterBuilder(builder)
        .withBaseUrl(URI.create("http://localhost:8080"))
        .withSerializer(new GsonSerializer())
        .build();

    Promise<TokenResponse> token = rest.post("/auth/login")
        .withRequestBody(() -> new LoginRequest(username, password))
        .withResponseType(TokenResponse.class)
        .promiseResponse();

    rest.get("/api/me")
        .withBearerTokenClient(() -> token.resolve().accessToken)
        .assertSuccess();
});
```

### SSR adapter

The SSR adapter targets **server-side rendered HTML** pages. It uses a shared `CookieManager` per adapter instance, so session cookies automatically persist across steps — enabling login → authenticated page flows without any manual session handling.

```java
Xcepto.given(scenario, builder -> {
    var ssr = new SsrAdapterBuilder(builder)
        .withBaseUrl(URI.create("http://localhost:8080"))
        .build();

    ssr.post("/auth/register")
        .withFormContent(SsrExtensions.toForm(new RegisterRequest(username, password)))
        .assertSuccess()
        .assertThatResponseContentString(html -> !html.contains("id=\"errors\""));

    ssr.post("/auth/login")
        .withFormContent(SsrExtensions.toForm(new LoginRequest(username, password)))
        .assertSuccess();

    // Cookie is automatically carried — no manual session management
    ssr.get("/")
        .assertThatResponseContentString(html -> html.contains(username));
});
```

`SsrExtensions.toForm(Object)` converts any POJO or record to a form-encoded `Map<String, String>` via reflection.

PATCH, PUT and DELETE are accessed via `ssr.request(path, HttpMethodVerb.PATCH)`.

#### Combined REST & SSR

```java
Xcepto.given(scenario, builder -> {
    var ssr = new SsrAdapterBuilder(builder).withBaseUrl(guiAddress).build();
    var rest = new RestAdapterBuilder(builder).withBaseUrl(apiAddress).withSerializer(serializer).build();

    Promise<String> pageHtml = ssr.post("/token/create")
        .withFormContent(SsrExtensions.toForm(new TokenCreateRequest("deploy-key")))
        .assertSuccess()
        .promiseResponse();

    rest.post("/api/env/create")
        .withBearerTokenClient(() -> extractToken(pageHtml.resolve()))
        .assertSuccess();
});
```

### RabbitMQ adapter

The RabbitMQ adapter listens for messages on a queue and transitions when a matching event arrives.

```java
Xcepto.given(scenario, builder -> {
    var rabbitmq = builder.registerAdapter(
        new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(scenario.getPort("rabbitmq", 5672))));

    var rest = new RestAdapterBuilder(builder)
        .withBaseUrl(URI.create("http://localhost:8081"))
        .withSerializer(new GsonSerializer())
        .build();

    rest.post("/shipment/accept")
        .withRequestBody(() -> new AcceptShipmentRequest(50))
        .withResponseType(AcceptShipmentResponse.class)
        .assertThatResponse((AcceptShipmentResponse r) -> r.amount == 50);

    // Transitions when this event arrives on the bus
    rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total == 50);
});
```

### Custom adapters

Predefined adapters cover common cases. For complex systems, custom adapters provide full flexibility.

<details>
<summary><strong>Advanced: Build your own adapter</strong></summary>

Extend `XceptoAdapter` and call `addStep(XceptoState)` to register steps:

```java
public class MyServiceAdapter extends XceptoAdapter {
    private final MyServiceClient client;

    public MyServiceAdapter(MyServiceClient client) {
        this.client = client;
    }

    public void performAction(Runnable action) {
        addStep(new XceptoState("My Action") {
            @Override
            public void onEnter(ServiceProvider sp) {
                action.run();
            }

            @Override
            public boolean evaluateConditionsForTransition(ServiceProvider sp) {
                return true; // always transition
            }
        });
    }

    public void expectEvent(Predicate<MyEvent> predicate) {
        addStep(new XceptoState("My Expectation") {
            @Override
            public boolean evaluateConditionsForTransition(ServiceProvider sp) {
                return client.pollEvent(predicate); // retry until matched
            }
        });
    }

    @Override protected void initialize(ServiceProvider sp) { client.connect(); }
    @Override protected void terminate() { client.disconnect(); }
    @Override protected void addServices(ServiceCollection sc) {}
}
```

For custom HTTP adapters, extend `AbstractHttpStateBuilderIdentity<T>` from `xceptoj-http` to get the full shared fluent builder API (assertions, query arguments, retry control, URL building) and `AbstractHttpState` for the execution model.

</details>

## Modules

| Artifact | Purpose |
|---|---|
| `org.xcepto:xceptoj` | Core framework — state machine, lifecycle, scenarios |
| `org.xcepto:xceptoj-http` | Shared HTTP abstractions — base builder and base state for custom HTTP adapters |
| `org.xcepto:xceptoj-rest` | REST adapter — JSON APIs with serializer, bearer token, typed responses |
| `org.xcepto:xceptoj-ssr` | SSR adapter — HTML pages with cookie persistence and form content |
| `org.xcepto:xceptoj-rabbitmq` | RabbitMQ adapter — event-driven message bus expectations |

## Resilience Guarantees

Xcepto is designed around a set of unconditional resilience properties:

- **Cleanup always runs** — adapters are terminated even if initialization or the test itself fails
- **Total timeout is enforced** — the budget covers scenario setup and initialization, not just test execution; no blocking call can prevent it
- **Exceptions short-circuit cleanly** — failures immediately stop execution and surface with specific error details
- **Propagated futures are checked** — fire-and-forget background tasks are checked for exceptions after each step
- **Logging is always flushed** — the `LoggingProvider` is flushed and closed even on exception
- **Transitions retry until timeout** — idempotent steps (GET, PUT, DELETE) are retried on assertion failure rather than failing the test immediately
