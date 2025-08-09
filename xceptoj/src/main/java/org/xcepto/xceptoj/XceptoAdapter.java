package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public abstract class XceptoAdapter {
  protected abstract void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException;

  protected abstract void addServices(ServiceCollection serviceCollection);

  protected abstract void terminate() throws XceptoAdapterTerminationException;

  protected abstract void injectBuilder(TransitionBuilder builder);
}
