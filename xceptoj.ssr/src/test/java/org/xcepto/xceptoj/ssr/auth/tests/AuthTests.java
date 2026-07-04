package org.xcepto.xceptoj.ssr.auth.tests;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;
import org.xcepto.xceptoj.ssr.scenarios.LocalHttpScenario;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTests {

  private static final TimeoutConfig TIMEOUT = new TimeoutConfig(Duration.ofSeconds(10), Duration.ofSeconds(5));

  @Test
  void cookiesPersistedAcrossSteps_authenticatedContentVisible() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/login", (form, headers, cookies) -> {
      String user = form.getOrDefault("username", "");
      return new LocalHttpScenario.Response(200, "<html>logged in</html>", Map.of("session", user));
    });
    scenario.addRoute("/profile", (form, headers, cookies) -> {
      String user = cookies.getOrDefault("session", "guest");
      return new LocalHttpScenario.Response(200, "<html>Profile: " + user + "</html>");
    });

    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();

      ssr.post("/login")
          .withFormContent(Map.of("username", "alice"))
          .assertSuccess();

      ssr.get("/profile")
          .assertSuccess()
          .assertThatResponseContentString(html -> html.contains("Profile: alice"));
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void withoutLogin_profileShowsGuest() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/profile", (form, headers, cookies) -> {
      String user = cookies.getOrDefault("session", "guest");
      return new LocalHttpScenario.Response(200, "<html>Profile: " + user + "</html>");
    });

    Xcepto.given(scenario, builder -> {
      var ssr = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      ssr.get("/profile")
          .assertSuccess()
          .assertThatResponseContentString(html -> html.contains("Profile: guest"));
    }, TIMEOUT, Duration.ofMillis(50));
  }

  @Test
  void cookiesNotSharedBetweenAdapterInstances() throws Exception {
    var scenario = new LocalHttpScenario();
    scenario.addRoute("/login", (form, headers, cookies) ->
        new LocalHttpScenario.Response(200, "<html>ok</html>", Map.of("session", "alice")));
    scenario.addRoute("/profile", (form, headers, cookies) -> {
      String user = cookies.getOrDefault("session", "guest");
      return new LocalHttpScenario.Response(200, "<html>Profile: " + user + "</html>");
    });

    Xcepto.given(scenario, builder -> {
      var ssrA = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();
      var ssrB = new SsrAdapterBuilder(builder).withBaseUrl(scenario.baseUri).build();

      ssrA.post("/login").assertSuccess();

      // ssrB has its own CookieManager — no session cookie
      ssrB.get("/profile")
          .assertSuccess()
          .assertThatResponseContentString(html -> html.contains("Profile: guest"));
    }, TIMEOUT, Duration.ofMillis(50));
  }
}
