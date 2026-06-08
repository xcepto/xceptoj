package org.xcepto.xceptoj.exceptiondetail.adapters;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingInitAdapter extends CleanAdapter {
  @Override
  protected void initialize(ServiceProvider serviceProvider) {
    throw new SampleFailure();
  }
}
