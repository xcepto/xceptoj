package org.xcepto.xceptoj.cleanupexecution.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.cleanupexecution.CleanupFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingTransitionState extends XceptoState {
  public FailingTransitionState() {
    super("failing transition");
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    throw new CleanupFailure();
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) {
  }
}
