package org.xcepto.xceptoj.cleanupexecution.scenarios;

import org.xcepto.xceptoj.cleanupexecution.CleanupFailure;

public class FailingSetupScenario extends TrackableCleanupScenario {
  @Override
  public void setupEnvironment() {
    throw new CleanupFailure();
  }
}
