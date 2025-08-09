package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public abstract class XceptoState {

  private final String name;
  private XceptoState nextXceptoState;

  public XceptoState(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public abstract boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) throws XceptoTestFailedException;

  public abstract void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException;

  public XceptoState getNextState() {
    return nextXceptoState;
  }

  public void setNextState(XceptoState state) {
    nextXceptoState = state;
  }

  public String getName() {
    return name;
  }
}
