package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptiondetail.scenarios.CleanScenario;
import org.xcepto.xceptoj.exceptiondetail.states.NeverTransitionState;
import org.xcepto.xceptoj.exceptions.TestTimeoutException;

class StateTimeoutExceptionTests {
  @Test
  void stalledTransitionThrowsTestTimeoutException() {
    assertThrows(TestTimeoutException.class,
        () -> Xcepto.given(
            new CleanScenario(),
            builder -> builder.addStep(new NeverTransitionState("never")),
            Duration.ofMillis(25),
            Duration.ofMillis(5)));
  }
}
