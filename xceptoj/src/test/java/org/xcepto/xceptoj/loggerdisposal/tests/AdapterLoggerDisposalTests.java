package org.xcepto.xceptoj.loggerdisposal.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.loggerdisposal.adapters.CleanupExceptionAdapter;
import org.xcepto.xceptoj.loggerdisposal.adapters.InitExceptionAdapter;
import org.xcepto.xceptoj.loggerdisposal.provider.MockedLoggingProvider;
import org.xcepto.xceptoj.loggerdisposal.scenarios.MockedLoggingScenario;

class AdapterLoggerDisposalTests {

  @Test
  void loggerFlushesAfterAdapterInitFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(
          new MockedLoggingScenario(provider),
          builder -> builder.registerAdapter(new InitExceptionAdapter(message)));
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }

  @Test
  void loggerFlushesAfterAdapterCleanupFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(
          new MockedLoggingScenario(provider),
          builder -> builder.registerAdapter(new CleanupExceptionAdapter(message)));
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }
}
