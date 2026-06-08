package org.xcepto.xceptoj.exceptiondetail.scenarios;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingSetupScenario extends CleanScenario {
  @Override
  public void setupEnvironment() {
    throw new SampleFailure();
  }
}
