package org.xcepto.xceptoj.rest.staticpath.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;
import org.xcepto.xceptoj.rest.scenarios.LocalHttpScenario;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticPathTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  private static LocalHttpScenario makeScenario() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/get", 200, "{}");
    scenario.addRoute("/api/post", 200, "{}");
    scenario.addRoute("/api/patch", 200, "{}");
    scenario.addRoute("/api/put", 200, "{}");
    scenario.addRoute("/api/delete", 200, "{}");
    scenario.addRoute("/api/error", 500, "{}");
    return scenario;
  }

  @Test
  void staticGet_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.get("/api/get").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPost_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.post("/api/post").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPatch_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.patch("/api/patch").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPut_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.put("/api/put").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticDelete_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.delete("/api/delete").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void serverErrorResponse_assertSuccessFails() {
    var scenario = makeScenario();
    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
          rest.get("/api/error").assertSuccess();
        }, TIMEOUT, Duration.ofMillis(50)));
  }

  @Test
  void serverErrorResponse_assertServerFailure() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      rest.get("/api/error").assertServerFailure();
    }, TIMEOUT, Duration.ofMillis(50));
  }
}
