package org.xcepto.xceptoj.loggerdisposal.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class TransitionExceptionState extends XceptoState {
  private final String message;

  public TransitionExceptionState(String name, String message) {
    super(name);
    this.message = message;
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    serviceProvider.get(LoggingProvider.class).logDebug(message);
    throw new RuntimeException("Intentional transition failure");
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
  }
}
