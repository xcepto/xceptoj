package org.xcepto.xceptoj.sequential.states;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.sequential.services.StatefulService;

public class CheckState extends XceptoState {
  private final StatefulService service;
  private final StatefulService.State expected;

  public CheckState(String name, StatefulService service, StatefulService.State expected) {
    super(name);
    this.service = service;
    this.expected = expected;
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return service.getState() == expected;
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {
  }
}
