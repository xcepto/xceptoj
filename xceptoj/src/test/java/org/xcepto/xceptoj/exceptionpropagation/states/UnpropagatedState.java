package org.xcepto.xceptoj.exceptionpropagation.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.util.concurrent.CompletableFuture;

public class UnpropagatedState extends XceptoState {
  public UnpropagatedState(String name) {
    super(name);
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return true;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    CompletableFuture.runAsync(() -> { throw new PropagatedException(); });
  }
}
