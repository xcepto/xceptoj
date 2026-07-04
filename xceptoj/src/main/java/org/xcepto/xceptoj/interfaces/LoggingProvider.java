package org.xcepto.xceptoj.interfaces;

public interface LoggingProvider extends AutoCloseable {
  void logDebug(String message);

  default void flush() {
  }

  @Override
  default void close() {
  }
}
