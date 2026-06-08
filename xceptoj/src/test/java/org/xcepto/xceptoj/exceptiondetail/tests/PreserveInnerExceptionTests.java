package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.xcepto.xceptoj.exceptiondetail.tests.ExceptionAssertions.assertStageFailure;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.exceptiondetail.scenarios.FailingCleanupScenario;
import org.xcepto.xceptoj.exceptions.ScenarioCleanupException;

class PreserveInnerExceptionTests {
  @Test
  void originalExceptionIsPreservedAsCause() {
    assertStageFailure(ScenarioCleanupException.class, SampleFailure.class,
        () -> Xcepto.given(new FailingCleanupScenario(), builder -> {
        }));
  }
}
