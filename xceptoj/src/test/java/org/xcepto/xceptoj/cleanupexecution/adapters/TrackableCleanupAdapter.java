package org.xcepto.xceptoj.cleanupexecution.adapters;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class TrackableCleanupAdapter extends XceptoAdapter {
  private boolean cleanedUp;

  public boolean cleanedUp() {
    return cleanedUp;
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) {
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() {
    cleanedUp = true;
  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
  }
}
