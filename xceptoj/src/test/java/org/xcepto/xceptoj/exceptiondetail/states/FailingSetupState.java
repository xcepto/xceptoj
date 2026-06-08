package org.xcepto.xceptoj.exceptiondetail.states;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingSetupState extends PassingState {
  public FailingSetupState(String name) {
    super(name);
    throw new SampleFailure();
  }
}
