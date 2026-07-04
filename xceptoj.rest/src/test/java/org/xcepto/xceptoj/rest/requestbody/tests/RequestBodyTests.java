package org.xcepto.xceptoj.rest.requestbody.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;
import org.xcepto.xceptoj.rest.scenarios.GsonSerializer;
import org.xcepto.xceptoj.rest.scenarios.LocalHttpScenario;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestBodyTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  record EchoPayload(String value) {}

  @Test
  void postWithRequestBody_serverReceivesBody_assertThatResponsePasses() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/echo", (body, headers) -> new LocalHttpScenario.Response(200, body));

    Xcepto.given(scenario, builder -> {
      var rest = new RestAdapterBuilder(builder)
          .withBaseUrl(scenario.baseUri)
          .withSerializer(new GsonSerializer())
          .build();
      rest.post("/api/echo")
          .withRequestBody(() -> new EchoPayload("hello"))
          .withResponseType(EchoPayload.class)
          .assertThatResponse((EchoPayload r) -> r.value().equals("hello"));
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void postWithRequestBody_assertThatResponseFails_whenPredicateFalse() {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/api/echo", (body, headers) -> new LocalHttpScenario.Response(200, body));

    assertThrows(XceptoTestFailedException.class, () ->
        Xcepto.given(scenario, builder -> {
          var rest = new RestAdapterBuilder(builder)
              .withBaseUrl(scenario.baseUri)
              .withSerializer(new GsonSerializer())
              .build();
          rest.post("/api/echo")
              .withRequestBody(() -> new EchoPayload("hello"))
              .withResponseType(EchoPayload.class)
              .assertThatResponse((EchoPayload r) -> r.value().equals("wrong"));
        }, TIMEOUT, Duration.ofMillis(50)));
  }
}
