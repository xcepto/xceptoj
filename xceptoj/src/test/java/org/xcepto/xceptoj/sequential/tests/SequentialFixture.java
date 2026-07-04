package org.xcepto.xceptoj.sequential.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.sequential.adapters.StatefulAdapter;
import org.xcepto.xceptoj.sequential.scenarios.SequentialScenario;
import org.xcepto.xceptoj.sequential.services.StatefulService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SequentialFixture {

  private final SequentialScenario scenario = new SequentialScenario();

  @AfterAll
  void teardown() throws Exception {
    scenario.teardown();
  }

  @Test
  @Order(1)
  void reusedScenarioStartsInInitialState() throws Exception {
    Xcepto.given(scenario, builder -> {
      var adapter = builder.registerAdapter(new StatefulAdapter(scenario.service));
      adapter.expectState(StatefulService.State.A);
    });
  }

  @Test
  @Order(2)
  void reusedScenarioPreservesServiceStateAfterAdvance() throws Exception {
    Xcepto.given(scenario, builder -> {
      var adapter = builder.registerAdapter(new StatefulAdapter(scenario.service));
      adapter.expectState(StatefulService.State.A);
      adapter.advanceState();
      adapter.expectState(StatefulService.State.B);
    });
  }

  @Test
  @Order(3)
  void reusedScenarioPreservesStateAcrossMultipleGivenCalls() throws Exception {
    Xcepto.given(scenario, builder -> {
      var adapter = builder.registerAdapter(new StatefulAdapter(scenario.service));
      adapter.expectState(StatefulService.State.B);
      adapter.advanceState();
      adapter.expectState(StatefulService.State.C);
    });
  }
}
