package org.xcepto.xceptoj.loggerdisposal.scenarios;

import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.loggerdisposal.provider.MockedLoggingProvider;

public class LoggingScenario extends Scenario {
  private final MockedLoggingProvider provider;
  private final String message;
  private final boolean failOnInit;

  public LoggingScenario(MockedLoggingProvider provider, String message, boolean failOnInit) {
    this.provider = provider;
    this.message = message;
    this.failOnInit = failOnInit;
  }

  @Override
  public void initializeEnvironment() {
    provider.logDebug(message);
    if (failOnInit) {
      throw new RuntimeException("Intentional init failure");
    }
  }

  @Override
  public void stopEnvironment() {
  }

  @Override
  public LoggingProvider createLoggingProvider() {
    return provider;
  }
}
