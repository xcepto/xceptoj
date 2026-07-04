package org.xcepto.xceptoj.exceptionpropagation.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;
import org.xcepto.xceptoj.exceptionpropagation.scenarios.FireAndForgetScenario;
import org.xcepto.xceptoj.exceptionpropagation.scenarios.SimpleSyncScenario;

class ScenarioExceptionPropagationTests {

  @Test
  void fireAndForgetFailureFromScenarioFailsTest() {
    assertThrows(PropagatedException.class,
        () -> Xcepto.given(new FireAndForgetScenario(), builder -> { }));
  }

  @Test
  void scenarioWithoutFireAndForgetDoesNotFailTest() {
    assertDoesNotThrow(() ->
        Xcepto.given(new SimpleSyncScenario(), builder -> { }));
  }
}
