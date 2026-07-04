package org.xcepto.xceptoj.exceptionpropagation.adapters;

import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.util.concurrent.CompletableFuture;

public class UnpropagatedAdapter extends XceptoAdapter {
  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {
    CompletableFuture.runAsync(() -> { throw new PropagatedException(); });
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() {
  }
}
