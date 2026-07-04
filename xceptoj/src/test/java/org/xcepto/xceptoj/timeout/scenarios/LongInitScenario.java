package org.xcepto.xceptoj.timeout.scenarios;

import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

public class LongInitScenario extends Scenario {

  @Override
  public void initializeEnvironment() throws XceptoScenarioResetException {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void stopEnvironment() {
  }
}
