package org.xcepto.xceptoj.sequential.scenarios;

import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.sequential.services.StatefulService;

public class SequentialScenario extends Scenario {
  public final StatefulService service = new StatefulService();

  @Override
  public void stopEnvironment() {
  }
}
