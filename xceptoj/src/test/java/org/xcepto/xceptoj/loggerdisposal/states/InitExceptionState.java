package org.xcepto.xceptoj.loggerdisposal.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.StateInitException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class InitExceptionState extends XceptoState {
  private final String message;

  public InitExceptionState(String name, String message) {
    super(name);
    this.message = message;
  }

  @Override
  public void initialize(ServiceProvider serviceProvider) throws StateInitException {
    serviceProvider.get(LoggingProvider.class).logDebug(message);
    throw new RuntimeException("Intentional state init failure");
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return false;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
  }
}
