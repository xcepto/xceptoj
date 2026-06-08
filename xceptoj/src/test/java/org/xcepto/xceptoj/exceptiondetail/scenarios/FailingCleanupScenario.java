package org.xcepto.xceptoj.exceptiondetail.scenarios;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingCleanupScenario extends CleanScenario {
  @Override
  public void stopEnvironment() {
    throw new SampleFailure();
  }
}
