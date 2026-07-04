package org.xcepto.xceptoj.cleanupexecution.scenarios;

import org.xcepto.xceptoj.cleanupexecution.CleanupFailure;

public class FailingStartScenario extends TrackableCleanupScenario {
  @Override
  public void startEnvironment() {
    throw new CleanupFailure();
  }
}
