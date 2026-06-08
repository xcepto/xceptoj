package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class NeverTransitionState extends PassingState {
  public NeverTransitionState(String name) {
    super(name);
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return false;
  }
}
