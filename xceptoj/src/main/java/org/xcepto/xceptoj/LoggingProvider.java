package org.xcepto.xceptoj;

public class LoggingProvider implements org.xcepto.xceptoj.interfaces.LoggingProvider {
  @Override
  public void logDebug(String message) {
    System.out.println(message);
  }
}
