package org.xcepto.xceptoj.loggerdisposal.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.loggerdisposal.provider.MockedLoggingProvider;
import org.xcepto.xceptoj.loggerdisposal.scenarios.MockedLoggingScenario;
import org.xcepto.xceptoj.loggerdisposal.states.EnterExceptionState;
import org.xcepto.xceptoj.loggerdisposal.states.InitExceptionState;
import org.xcepto.xceptoj.loggerdisposal.states.TransitionExceptionState;

class StateLoggerDisposalTests {

  @Test
  void loggerFlushesAfterStateInitFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(
          new MockedLoggingScenario(provider),
          builder -> builder.addStep(new InitExceptionState("state", message)));
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }

  @Test
  void loggerFlushesAfterStateTransitionFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(
          new MockedLoggingScenario(provider),
          builder -> builder.addStep(new TransitionExceptionState("state", message)));
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }

  @Test
  void loggerFlushesAfterStateEnterFailure() {
    String message = UUID.randomUUID().toString();
    MockedLoggingProvider provider = new MockedLoggingProvider();

    try {
      Xcepto.given(
          new MockedLoggingScenario(provider),
          builder -> builder.addStep(new EnterExceptionState("state", message)));
    } catch (Exception ignored) {
    }

    assertTrue(provider.wasFlushed(message));
  }
}
