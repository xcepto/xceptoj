package org.xcepto.xceptoj.timeout.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.TotalTimeoutException;
import org.xcepto.xceptoj.timeout.scenarios.LongInitScenario;
import org.xcepto.xceptoj.timeout.scenarios.LongSetupScenario;

class TotalTimeoutInterruptTests {

  @Test
  void totalTimeoutInterruptsScenarioSetup() {
    assertThrows(TotalTimeoutException.class,
        () -> Xcepto.given(
            new LongSetupScenario(),
            builder -> { },
            new TimeoutConfig(Duration.ofMillis(200), Duration.ofSeconds(30)),
            Duration.ofMillis(10)));
  }

  @Test
  void totalTimeoutInterruptsScenarioInit() {
    assertThrows(TotalTimeoutException.class,
        () -> Xcepto.given(
            new LongInitScenario(),
            builder -> { },
            new TimeoutConfig(Duration.ofMillis(200), Duration.ofSeconds(30)),
            Duration.ofMillis(10)));
  }
}
