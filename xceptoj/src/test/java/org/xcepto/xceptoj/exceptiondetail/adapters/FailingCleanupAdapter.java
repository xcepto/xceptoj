package org.xcepto.xceptoj.exceptiondetail.adapters;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingCleanupAdapter extends CleanAdapter {
  @Override
  protected void terminate() {
    throw new SampleFailure();
  }
}
