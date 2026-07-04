package org.xcepto.xceptoj.ssr.formcontent.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;
import org.xcepto.xceptoj.ssr.extensions.SsrExtensions;
import org.xcepto.xceptoj.ssr.scenarios.LocalHttpScenario;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FormContentTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  record GreetRequest(String name) {}

  @Test
  void postWithFormContent_serverReceivesFields_assertThatResponsePasses() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/greet", (form, headers, cookies) ->
        new LocalHttpScenario.Response(200, "<html>Hello " + form.getOrDefault("name", "") + "</html>"));

    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.post("/api/greet")
          .withFormContent(SsrExtensions.toForm(new GreetRequest("alice")))
          .assertSuccess()
          .assertThatResponseContentString(html -> html.contains("Hello alice"));
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void postWithFormContent_assertThatResponseFails_whenContentDoesNotMatch() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/greet", (form, headers, cookies) ->
        new LocalHttpScenario.Response(200, "<html>Hello " + form.getOrDefault("name", "") + "</html>"));

    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
          ssr.post("/api/greet")
              .withFormContent(SsrExtensions.toForm(new GreetRequest("alice")))
              .assertSuccess()
              .assertThatResponseContentString(html -> html.contains("Hello bob"));
        }, TIMEOUT, Duration.ofMillis(50)));
  }

  @Test
  void postWithLazyFormContent_evaluatedAtExecutionTime() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/echo", (form, headers, cookies) ->
        new LocalHttpScenario.Response(200, "<html>" + form.getOrDefault("msg", "") + "</html>"));

    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      String[] captured = {"lazy"};
      ssr.post("/api/echo")
          .withFormContent(() -> Map.of("msg", captured[0]))
          .assertThatResponseContentString(html -> html.contains("lazy"));
    }, TIMEOUT, Duration.ofMillis(50));
  }
}
