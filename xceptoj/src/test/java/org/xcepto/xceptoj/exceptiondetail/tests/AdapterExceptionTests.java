package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.xcepto.xceptoj.exceptiondetail.tests.ExceptionAssertions.assertStageFailure;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.exceptiondetail.adapters.FailingCleanupAdapter;
import org.xcepto.xceptoj.exceptiondetail.adapters.FailingConstructionAdapter;
import org.xcepto.xceptoj.exceptiondetail.adapters.FailingInitAdapter;
import org.xcepto.xceptoj.exceptiondetail.scenarios.CleanScenario;
import org.xcepto.xceptoj.exceptions.AdapterCleanupException;
import org.xcepto.xceptoj.exceptions.AdapterInitException;
import org.xcepto.xceptoj.exceptions.ArrangeTestException;

class AdapterExceptionTests {
  @Test
  void failingSetupThrowsArrangeTestException() {
    assertStageFailure(ArrangeTestException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.registerAdapter(new FailingConstructionAdapter())));
  }

  @Test
  void failingInitThrowsAdapterInitException() {
    assertStageFailure(AdapterInitException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.registerAdapter(new FailingInitAdapter())));
  }

  @Test
  void failingCleanupThrowsAdapterCleanupException() {
    assertStageFailure(AdapterCleanupException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.registerAdapter(new FailingCleanupAdapter())));
  }
}
