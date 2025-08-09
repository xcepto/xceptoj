package org.xcepto.xceptoj.interfaces;

public interface ServiceProvider {
  <T> T get(Class<T> tClass);

}
