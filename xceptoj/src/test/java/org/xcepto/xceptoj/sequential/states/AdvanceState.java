package org.xcepto.xceptoj.sequential.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.sequential.services.StatefulService;

public class AdvanceState extends XceptoState {
  private final StatefulService service;

  public AdvanceState(String name, StatefulService service) {
    super(name);
    this.service = service;
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return true;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    service.advance();
  }
}
