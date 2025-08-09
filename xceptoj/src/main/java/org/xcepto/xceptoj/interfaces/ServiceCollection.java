package org.xcepto.xceptoj.interfaces;

import jakarta.inject.Provider;

public interface ServiceCollection {
  ServiceProvider buildServiceProvider();

  <T> void register(Class<T> type, Provider<T> provider);

}
