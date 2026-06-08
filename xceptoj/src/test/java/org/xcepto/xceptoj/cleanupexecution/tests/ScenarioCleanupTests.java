package org.xcepto.xceptoj.cleanupexecution.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.cleanupexecution.scenarios.FailingInitScenario;
import org.xcepto.xceptoj.cleanupexecution.scenarios.FailingSetupScenario;
import org.xcepto.xceptoj.cleanupexecution.scenarios.FailingStartScenario;
import org.xcepto.xceptoj.cleanupexecution.scenarios.TrackableCleanupScenario;
import org.xcepto.xceptoj.cleanupexecution.states.FailingTransitionState;
import org.xcepto.xceptoj.exceptions.ScenarioInitException;
import org.xcepto.xceptoj.exceptions.ScenarioSetupException;
import org.xcepto.xceptoj.exceptions.StateTransitionException;

class ScenarioCleanupTests {
  @Test
  void scenarioStopRunsAfterSuccessfulGiven() throws Exception {
    var scenario = new TrackableCleanupScenario();

    Xcepto.given(scenario, builder -> {
    });

    assertTrue(scenario.cleanupRan());
  }

  @Test
  void scenarioStopRunsAfterScenarioSetupFailure() {
    var scenario = new FailingSetupScenario();

    assertThrows(ScenarioSetupException.class, () -> Xcepto.given(scenario, builder -> {
    }));

    assertTrue(scenario.cleanupRan());
  }

  @Test
  void scenarioStopRunsAfterScenarioStartFailure() {
    var scenario = new FailingStartScenario();

    assertThrows(ScenarioInitException.class, () -> Xcepto.given(scenario, builder -> {
    }));

    assertTrue(scenario.cleanupRan());
  }

  @Test
  void scenarioStopRunsAfterScenarioInitFailure() {
    var scenario = new FailingInitScenario();

    assertThrows(ScenarioInitException.class, () -> Xcepto.given(scenario, builder -> {
    }));

    assertTrue(scenario.cleanupRan());
  }

  @Test
  void scenarioStopRunsAfterTestFailure() {
    var scenario = new TrackableCleanupScenario();

    assertThrows(StateTransitionException.class,
        () -> Xcepto.given(scenario, builder -> builder.addStep(new FailingTransitionState())));

    assertTrue(scenario.cleanupRan());
  }
}
