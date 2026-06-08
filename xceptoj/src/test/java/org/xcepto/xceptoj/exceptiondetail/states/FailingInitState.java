package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingInitState extends PassingState {
  public FailingInitState(String name) {
    super(name);
  }

  @Override
  public void initialize(ServiceProvider serviceProvider) {
    throw new SampleFailure();
  }
}
