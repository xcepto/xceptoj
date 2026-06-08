package org.xcepto.xceptoj.exceptiondetail.adapters;

import org.xcepto.xceptoj.exceptiondetail.SampleFailure;

public class FailingConstructionAdapter extends CleanAdapter {
  public FailingConstructionAdapter() {
    throw new SampleFailure();
  }
}
