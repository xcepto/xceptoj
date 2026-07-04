package org.xcepto.xceptoj.ssr.staticpath.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;
import org.xcepto.xceptoj.ssr.scenarios.LocalHttpScenario;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StaticPathTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  private static LocalHttpScenario makeScenario() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/get", 200, "<html>ok</html>");
    scenario.addRoute("/api/post", 200, "<html>ok</html>");
    scenario.addRoute("/api/patch", 200, "<html>ok</html>");
    scenario.addRoute("/api/put", 200, "<html>ok</html>");
    scenario.addRoute("/api/delete", 200, "<html>ok</html>");
    scenario.addRoute("/api/error", 500, "<html>error</html>");
    return scenario;
  }

  @Test
  void staticGet_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.get("/api/get").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPost_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.post("/api/post").assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPatch_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.request("/api/patch", HttpMethodVerb.PATCH).assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticPut_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.request("/api/put", HttpMethodVerb.PUT).assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void staticDelete_assertSuccess() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.request("/api/delete", HttpMethodVerb.DELETE).assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void serverErrorResponse_assertSuccessFails() {
    var scenario = makeScenario();
    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
          ssr.get("/api/error").assertSuccess();
        }, TIMEOUT, Duration.ofMillis(50)));
  }

  @Test
  void serverErrorResponse_assertServerFailure() throws Exception {
    var scenario = makeScenario();
    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.get("/api/error").assertServerFailure();
    }, TIMEOUT, Duration.ofMillis(50));
  }
}
