package org.xcepto.xceptoj.cleanupexecution.adapters;

import org.xcepto.xceptoj.cleanupexecution.CleanupFailure;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class FailingInitAdapter extends TrackableCleanupAdapter {
  @Override
  protected void initialize(ServiceProvider serviceProvider) {
    throw new CleanupFailure();
  }
}
