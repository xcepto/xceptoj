package org.xcepto.xceptoj.sequential.adapters;

import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.sequential.services.StatefulService;
import org.xcepto.xceptoj.sequential.states.AdvanceState;
import org.xcepto.xceptoj.sequential.states.CheckState;

public class StatefulAdapter extends XceptoAdapter {
  private final StatefulService service;

  public StatefulAdapter(StatefulService service) {
    this.service = service;
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() {
  }

  public void expectState(StatefulService.State expected) {
    addStep(new CheckState("expect " + expected, service, expected));
  }

  public void advanceState() {
    addStep(new AdvanceState("advance", service));
  }
}
