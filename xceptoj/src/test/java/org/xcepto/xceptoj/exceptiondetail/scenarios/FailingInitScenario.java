package org.xcepto.xceptoj.exceptiondetail.scenarios;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingInitScenario extends CleanScenario {
  @Override
  public void initializeEnvironment() {
    throw new SampleFailure();
  }
}
