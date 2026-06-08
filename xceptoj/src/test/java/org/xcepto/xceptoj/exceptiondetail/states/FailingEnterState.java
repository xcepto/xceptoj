package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingEnterState extends PassingState {
  public FailingEnterState(String name) {
    super(name);
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) {
    throw new SampleFailure();
  }
}
