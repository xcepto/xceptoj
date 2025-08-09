package org.xcepto.xceptoj.di;

import org.xcepto.xceptoj.interfaces.ServiceCollection;
import jakarta.inject.Provider;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider implements ServiceCollection, org.xcepto.xceptoj.interfaces.ServiceProvider {
  private final Map<Class<?>, Provider<?>> services = new HashMap<>();

  public <T> void register(Class<T> type, Provider<T> provider) {
    services.put(type, provider);
  }

  public <T> T get(Class<T> type) {
    return type.cast(services.get(type).get());
  }

  @Override
  public org.xcepto.xceptoj.interfaces.ServiceProvider buildServiceProvider() {
    return this;
  }
}
