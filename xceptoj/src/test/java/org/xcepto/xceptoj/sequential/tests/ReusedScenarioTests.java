package org.xcepto.xceptoj.sequential.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.sequential.adapters.StatefulAdapter;
import org.xcepto.xceptoj.sequential.scenarios.SequentialScenario;
import org.xcepto.xceptoj.sequential.services.StatefulService;

class ReusedScenarioTests {

  @Test
  void reusedScenarioDoesNotRestartEnvironmentAutomatically() throws Exception {
    SequentialScenario scenario = new SequentialScenario();

    Xcepto.given(scenario, builder -> {
      var adapter = builder.registerAdapter(new StatefulAdapter(scenario.service));
      adapter.advanceState();
    });

    assertEquals(StatefulService.State.B, scenario.service.getState());

    Xcepto.given(scenario, builder -> {
      var adapter = builder.registerAdapter(new StatefulAdapter(scenario.service));
      adapter.expectState(StatefulService.State.B);
    });
  }

  @Test
  void explicitScenarioTeardownResetsSetupFlags() throws Exception {
    SequentialScenario scenario = new SequentialScenario();

    Xcepto.given(scenario, builder -> { });
    scenario.teardown();

    assertDoesNotThrow(() ->
        Xcepto.given(scenario, builder -> { }));
  }
}
