package org.xcepto.xceptoj.cleanupexecution.scenarios;

import org.xcepto.xceptoj.cleanupexecution.CleanupFailure;

public class FailingInitScenario extends TrackableCleanupScenario {
  @Override
  public void initializeEnvironment() {
    throw new CleanupFailure();
  }
}
