package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.xcepto.xceptoj.exceptiondetail.tests.ExceptionAssertions.assertStageFailure;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.exceptiondetail.scenarios.FailingCleanupScenario;
import org.xcepto.xceptoj.exceptiondetail.scenarios.FailingInitScenario;
import org.xcepto.xceptoj.exceptiondetail.scenarios.FailingSetupScenario;
import org.xcepto.xceptoj.exceptions.ScenarioCleanupException;
import org.xcepto.xceptoj.exceptions.ScenarioInitException;
import org.xcepto.xceptoj.exceptions.ScenarioSetupException;

class ScenarioExceptionTests {
  @Test
  void failingSetupThrowsScenarioSetupException() {
    assertStageFailure(ScenarioSetupException.class, SampleFailure.class,
        () -> Xcepto.given(new FailingSetupScenario(), builder -> {
        }));
  }

  @Test
  void failingInitThrowsScenarioInitException() {
    assertStageFailure(ScenarioInitException.class, SampleFailure.class,
        () -> Xcepto.given(new FailingInitScenario(), builder -> {
        }));
  }

  @Test
  void failingCleanupThrowsScenarioCleanupException() {
    assertStageFailure(ScenarioCleanupException.class, SampleFailure.class,
        () -> Xcepto.given(new FailingCleanupScenario(), builder -> {
        }));
  }
}
