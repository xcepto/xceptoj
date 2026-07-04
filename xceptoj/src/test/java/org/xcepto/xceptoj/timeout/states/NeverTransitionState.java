package org.xcepto.xceptoj.timeout.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class NeverTransitionState extends XceptoState {
  public NeverTransitionState(String name) {
    super(name);
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return false;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
  }
}
