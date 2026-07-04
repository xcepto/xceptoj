package org.xcepto.xceptoj.ssr.promise.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.Promise;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;
import org.xcepto.xceptoj.ssr.scenarios.LocalHttpScenario;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PromiseTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  @Test
  void promisedHtmlBody_drivesNextPostFormContent() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/content", 200, "<html>greeting=hello world</html>");
    scenario.addRoute("/api/validate", (form, headers, cookies) -> {
      String received = form.getOrDefault("payload", "");
      boolean ok = received.contains("hello world");
      return new LocalHttpScenario.Response(ok ? 200 : 400, ok ? "<html>ok</html>" : "<html>bad</html>");
    });

    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();

      Promise<String> promise = ssr.get("/api/content")
          .assertSuccess()
          .promiseResponse();

      ssr.post("/api/validate")
          .withFormContent(() -> Map.of("payload", promise.resolve()))
          .assertSuccess();
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void promisedResponse_drivesNextRequest_assertSuccessFails_whenValidationRejects() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/content", 200, "<html>wrong content</html>");
    scenario.addRoute("/api/validate", (form, headers, cookies) ->
        new LocalHttpScenario.Response(400, "<html>rejected</html>"));

    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();

          Promise<String> promise = ssr.get("/api/content")
              .assertSuccess()
              .promiseResponse();

          ssr.post("/api/validate")
              .withFormContent(() -> Map.of("payload", promise.resolve()))
              .assertSuccess();
        }, TIMEOUT, Duration.ofMillis(50)));
  }
}
