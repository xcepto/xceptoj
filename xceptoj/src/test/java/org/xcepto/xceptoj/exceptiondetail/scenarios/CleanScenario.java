package org.xcepto.xceptoj.exceptiondetail.scenarios;

import org.xcepto.xceptoj.Scenario;

public class CleanScenario extends Scenario {
  @Override
  public void stopEnvironment() {
  }

  @Override
  public int getPort(String service, int port) {
    return port;
  }
}
