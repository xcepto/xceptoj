# xceptoj — Agent Guide

Java implementation of the Xcepto system testing framework. Targets Java 21, built with Gradle.

## Module structure

```
xceptoj/              — Core: Xcepto, Scenario, TransitionBuilder, XceptoAdapter, XceptoState
xceptoj.http/         — Shared HTTP state builder (AbstractHttpStateBuilderIdentity<T>)
xceptoj.rest/         — REST adapter (typed responses, bearer tokens, promises)
xceptoj.ssr/          — SSR adapter (cookie manager, form content, HTML assertions)
xceptoj.rabbitmq/     — RabbitMQ adapter (deprecated)
examples/warehouse/       — Runnable end-to-end example (warehouse microservices)
examples/docs.examples/   — Documentation example project (compile-only, not published)
```

## Docs example module

`examples/docs.examples/` contains all code snippets shown on xcepto.org, as JUnit5 tests marked `@Disabled("docs-example")`.

**Purpose:** Snippets shown in the docs must compile against the real framework. Before changing a docs snippet, change it here first and verify:
```
./gradlew :docs.examples:compileTestJava
```
Then copy the verified code into `xcepto-docs/src/pages/index.tsx` or the relevant `.md` file.

Source: `examples/docs.examples/src/test/java/org/xcepto/xceptoj/docs/HomepageSnippets.java`

## Key API facts

### REST adapter builder chain

```java
// builder is TransitionBuilder (passed into Xcepto.given lambda)
var rest = new RestAdapterBuilder(builder)
    .withBaseUrl(URI.create("http://..."))
    .withSerializer(new GsonSerializer())
    .build();

// Plain step (status assertions only)
rest.post("/path").withRequestBody(() -> new Req()).assertSuccess();

// Typed response — withResponseType returns DeserializedResponseRestStateBuilderIdentity<T>
rest.get("/path")
    .withResponseType(MyResponse.class)
    .assertThatResponse(r -> r.someField == expected);  // Predicate<T>

// IMPORTANT: explicit cast needed when Java can't resolve Predicate<T> vs HttpResponseAssertion
rest.get("/path")
    .withResponseType(ProfileResponse.class)
    .assertThatResponse((ProfileResponse r) -> r.username.equals(username));

// Promise
Promise<T> p = rest.post("/path")
    .withResponseType(T.class)
    .assertThatResponse(r -> ...)
    .promiseResponse();
// Consume lazily:
rest.post("/other").withBearerTokenClient(() -> p.resolve().accessToken);
```

### SSR adapter

```java
var ssr = new SsrAdapterBuilder(builder)
    .withBaseUrl(URI.create("http://..."))
    .build();  // Creates cookie-aware HttpClient automatically

ssr.post("/auth/login")
    .withFormContent(SsrExtensions.toForm(new LoginRequest(u, p)))
    .assertSuccess();

ssr.get("/dashboard")
    .assertThatResponseContentString(html -> html.contains(username));
```

### HTTP verb behavior

- POST / PATCH → executes once, immediate failure on assertion error
- GET / PUT / DELETE → retried until assertions pass or timeout expires

### Xcepto.given signature

```java
Xcepto.given(scenario, builder -> {
    // wire adapters and steps here
}, TimeoutConfig.fromSeconds(30), Duration.ofMillis(100));

// or with Duration (no separate test budget):
Xcepto.given(scenario, builder -> { ... }, Duration.ofSeconds(30), Duration.ofMillis(100));
```

### Scenario base

```java
public class MyScenario extends Scenario {
    public URI apiAddress;  // public fields for test access

    @Override
    public void setupEnvironment() throws XceptoScenarioResetException { ... }

    @Override
    public void stopEnvironment() throws XceptoScenarioResetException { ... }
}
```

### Custom adapter

```java
public class MyAdapter extends XceptoAdapter {
    private TransitionBuilder transitionBuilder;

    @Override
    protected void injectBuilder(TransitionBuilder builder) {
        this.transitionBuilder = builder;
    }

    public MyFlowBuilder myAction(String id) {
        return new MyFlowBuilder(transitionBuilder).withId(id);
    }

    // Required abstract implementations (can be empty if not needed):
    @Override protected void initialize(ServiceProvider sp) throws XceptoAdapterInitializationException {}
    @Override protected void addServices(ServiceCollection sc) {}
    @Override protected void terminate() throws XceptoAdapterTerminationException {}
}
```

## Build commands

```bash
./gradlew :docs.examples:compileTestJava   # validate docs snippets compile
./gradlew :xceptoj:test                    # run core tests
./gradlew build                            # full build
```
