package org.xcepto.xceptoj.loggerdisposal.adapters;

import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class CleanupExceptionAdapter extends XceptoAdapter {
  private final String message;

  public CleanupExceptionAdapter(String message) {
    this.message = message;
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) {
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() throws XceptoAdapterTerminationException {
    throw new RuntimeException("terminate() called without service provider");
  }

  @Override
  protected void terminate(ServiceProvider serviceProvider) throws XceptoAdapterTerminationException {
    serviceProvider.get(LoggingProvider.class).logDebug(message);
    throw new RuntimeException("Intentional adapter cleanup failure");
  }
}
