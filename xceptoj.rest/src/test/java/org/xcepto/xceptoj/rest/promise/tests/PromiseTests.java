package org.xcepto.xceptoj.rest.promise.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rest.Promise;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;
import org.xcepto.xceptoj.rest.scenarios.GsonSerializer;
import org.xcepto.xceptoj.rest.scenarios.LocalHttpScenario;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PromiseTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  record ResourceRef(String path) {}

  @Test
  void promisedResponse_drivesNextRequest_assertSuccess() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/first", 200, "{\"path\":\"/api/second\"}");
    scenario.addRoute("/api/second", 200, "{}");

    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder)
          .withBaseUrl(scenario.baseUri)
          .withSerializer(new GsonSerializer())
          .build();

      Promise<ResourceRef> promise = rest.get("/api/first")
          .withResponseType(ResourceRef.class)
          .promiseResponse();

      rest.get(() -> promise.resolve().path())
          .assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void promisedResponse_drivesNextRequest_assertSuccessFails_whenEndpointMissing() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/first", 200, "{\"path\":\"/api/missing\"}");

    var shortTimeout = new TimeoutConfig(Duration.ofMillis(500), Duration.ofMillis(200));
    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var rest = new RestAdapterBuilder(builder)
              .withBaseUrl(scenario.baseUri)
              .withSerializer(new GsonSerializer())
              .build();

          Promise<ResourceRef> promise = rest.get("/api/first")
              .withResponseType(ResourceRef.class)
              .promiseResponse();

          rest.get(() -> promise.resolve().path())
              .assertSuccess();
        }, shortTimeout, Duration.ofMillis(50)));
  }
}
