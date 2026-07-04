package org.xcepto.xceptoj.exceptionpropagation.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;
import org.xcepto.xceptoj.exceptionpropagation.scenarios.SimpleSyncScenario;
import org.xcepto.xceptoj.exceptionpropagation.states.PropagatedState;
import org.xcepto.xceptoj.exceptionpropagation.states.UnpropagatedState;

class StateExceptionPropagationTests {

  @Test
  void propagatedStateFutureFailsTest() {
    assertThrows(PropagatedException.class,
        () -> Xcepto.given(
            new SimpleSyncScenario(),
            builder -> builder.addStep(new PropagatedState("propagated"))));
  }

  @Test
  void unpropagatedStateFutureDoesNotFailTest() {
    assertDoesNotThrow(() ->
        Xcepto.given(
            new SimpleSyncScenario(),
            builder -> builder.addStep(new UnpropagatedState("unpropagated"))));
  }
}
