package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.xcepto.xceptoj.exceptiondetail.tests.ExceptionAssertions.assertStageFailure;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.exceptiondetail.scenarios.CleanScenario;
import org.xcepto.xceptoj.exceptiondetail.states.FailingEnterState;
import org.xcepto.xceptoj.exceptiondetail.states.FailingInitState;
import org.xcepto.xceptoj.exceptiondetail.states.FailingSetupState;
import org.xcepto.xceptoj.exceptiondetail.states.FailingTransitionState;
import org.xcepto.xceptoj.exceptions.ArrangeTestException;
import org.xcepto.xceptoj.exceptions.StateEnterException;
import org.xcepto.xceptoj.exceptions.StateInitException;
import org.xcepto.xceptoj.exceptions.StateTransitionException;

class StateExceptionTests {
  @Test
  void failingStateSetupThrowsArrangeTestException() {
    assertStageFailure(ArrangeTestException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.addStep(new FailingSetupState("setup"))));
  }

  @Test
  void failingStateInitThrowsStateInitException() {
    assertStageFailure(StateInitException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.addStep(new FailingInitState("init"))));
  }

  @Test
  void failingStateEnterThrowsStateEnterException() {
    assertStageFailure(StateEnterException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.addStep(new FailingEnterState("enter"))));
  }

  @Test
  void failingStateTransitionThrowsStateTransitionException() {
    assertStageFailure(StateTransitionException.class, SampleFailure.class,
        () -> Xcepto.given(new CleanScenario(), builder -> builder.addStep(new FailingTransitionState("transition"))));
  }
}
