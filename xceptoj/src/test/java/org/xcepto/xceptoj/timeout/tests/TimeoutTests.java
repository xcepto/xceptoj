package org.xcepto.xceptoj.timeout.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.TestTimeoutException;
import org.xcepto.xceptoj.exceptions.TotalTimeoutException;
import org.xcepto.xceptoj.timeout.scenarios.InstantaneousScenario;
import org.xcepto.xceptoj.timeout.states.NeverTransitionState;

class TimeoutTests {

  @Test
  void testTimeoutFailsWhenStateProgressStalls() {
    assertThrows(TestTimeoutException.class,
        () -> Xcepto.given(
            new InstantaneousScenario(),
            builder -> builder.addStep(new NeverTransitionState("never")),
            TimeoutConfig.fromSeconds(1.0f, 1.0f),
            Duration.ofMillis(10)));
  }

  @Test
  void totalTimeoutFailsWhenPollingExceedsTotalBudget() {
    assertThrows(TotalTimeoutException.class,
        () -> Xcepto.given(
            new InstantaneousScenario(),
            builder -> builder.addStep(new NeverTransitionState("never")),
            new TimeoutConfig(Duration.ofMillis(50), Duration.ofSeconds(30)),
            Duration.ofMillis(10)));
  }

  @Test
  void testBudgetExhaustsBeforeTotalBudgetThrowsTestTimeoutException() {
    assertThrows(TestTimeoutException.class,
        () -> Xcepto.given(
            new InstantaneousScenario(),
            builder -> builder.addStep(new NeverTransitionState("never")),
            new TimeoutConfig(Duration.ofSeconds(30), Duration.ofMillis(50)),
            Duration.ofMillis(10)));
  }

  @Test
  void emptyBuilderCompletesWithinBudget() {
    assertDoesNotThrow(() ->
        Xcepto.given(
            new InstantaneousScenario(),
            builder -> { },
            TimeoutConfig.fromSeconds(1.0f),
            Duration.ofMillis(10)));
  }
}
