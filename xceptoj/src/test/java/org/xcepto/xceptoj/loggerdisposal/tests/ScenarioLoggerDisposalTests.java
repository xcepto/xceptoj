package org.xcepto.xceptoj.loggerdisposal.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.loggerdisposal.provider.MockedLoggingProvider;
import org.xcepto.xceptoj.loggerdisposal.scenarios.LoggingScenario;

class ScenarioLoggerDisposalTests {

  @Test
  void loggerFlushesAfterScenarioInitFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(new LoggingScenario(provider, message, true), builder -> { });
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }

  @Test
  void loggerFlushesAfterSuccessfulRun() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(new LoggingScenario(provider, message, false), builder -> { });
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }
}
