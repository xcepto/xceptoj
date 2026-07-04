package org.xcepto.xceptoj.loggerdisposal.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class EnterExceptionState extends XceptoState {
  private final String message;

  public EnterExceptionState(String name, String message) {
    super(name);
    this.message = message;
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return true;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    serviceProvider.get(LoggingProvider.class).logDebug(message);
    throw new RuntimeException("Intentional enter failure");
  }
}
