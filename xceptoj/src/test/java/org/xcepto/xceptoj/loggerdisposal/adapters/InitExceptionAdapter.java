package org.xcepto.xceptoj.loggerdisposal.adapters;

import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class InitExceptionAdapter extends XceptoAdapter {
  private final String message;

  public InitExceptionAdapter(String message) {
    this.message = message;
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {
    serviceProvider.get(LoggingProvider.class).logDebug(message);
    throw new RuntimeException("Intentional adapter init failure");
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() {
  }
}
