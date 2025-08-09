package org.xcepto.xceptoj;

import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class UnconditionalXceptoState extends XceptoState {
  public UnconditionalXceptoState(String name) {
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
