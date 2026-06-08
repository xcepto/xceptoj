package org.xcepto.xceptoj.exceptiondetail.adapters;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class CleanAdapter extends XceptoAdapter {
  @Override
  protected void initialize(ServiceProvider serviceProvider) {
  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
  }

  @Override
  protected void terminate() {
  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
  }
}
