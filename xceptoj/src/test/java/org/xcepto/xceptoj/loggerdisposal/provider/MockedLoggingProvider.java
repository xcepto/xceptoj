package org.xcepto.xceptoj.loggerdisposal.provider;

import org.xcepto.xceptoj.interfaces.LoggingProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MockedLoggingProvider implements LoggingProvider {
  private final Queue<String> pending = new ArrayDeque<>();
  private final List<String> flushed = new ArrayList<>();

  @Override
  public void logDebug(String message) {
    pending.add(message);
  }

  @Override
  public void flush() {
    while (!pending.isEmpty()) {
      flushed.add(pending.poll());
    }
  }

  @Override
  public void close() {
    flush();
  }

  public boolean wasFlushed(String message) {
    return flushed.contains(message);
  }
}
