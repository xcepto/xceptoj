package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class PassingState extends XceptoState {
  public PassingState(String name) {
    super(name);
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return true;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) {
  }
}
