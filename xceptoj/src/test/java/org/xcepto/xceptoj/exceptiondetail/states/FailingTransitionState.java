package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingTransitionState extends PassingState {
  public FailingTransitionState(String name) {
    super(name);
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    throw new SampleFailure();
  }
}
