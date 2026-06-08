package org.xcepto.xceptoj.cleanupexecution.scenarios;

import org.xcepto.xceptoj.Scenario;

public class TrackableCleanupScenario extends Scenario {
  private boolean cleanupRan;

  public boolean cleanupRan() {
    return cleanupRan;
  }

  @Override
  public void stopEnvironment() {
    cleanupRan = true;
  }

  @Override
  public int getPort(String service, int port) {
    return port;
  }
}
