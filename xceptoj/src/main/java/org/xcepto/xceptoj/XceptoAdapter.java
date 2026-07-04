package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.util.concurrent.CompletableFuture;

public abstract class XceptoAdapter {
  private TransitionBuilder _builder;

  protected abstract void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException;

  protected abstract void addServices(ServiceCollection serviceCollection);

  protected abstract void terminate() throws XceptoAdapterTerminationException;

  protected void terminate(ServiceProvider serviceProvider) throws XceptoAdapterTerminationException {
    terminate();
  }

  protected void injectBuilder(TransitionBuilder builder) {
    _builder = builder;
  }

  protected void propagateExceptions(CompletableFuture<?> future) {
    _builder.propagateExceptions(future);
  }

  protected void addStep(XceptoState state) {
    _builder.addStep(state);
  }
}
