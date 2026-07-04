package org.xcepto.xceptoj.loggerdisposal.scenarios;

import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.loggerdisposal.provider.MockedLoggingProvider;

public class MockedLoggingScenario extends Scenario {
  private final MockedLoggingProvider provider;

  public MockedLoggingScenario(MockedLoggingProvider provider) {
    this.provider = provider;
  }

  @Override
  public void stopEnvironment() {
  }

  @Override
  public LoggingProvider createLoggingProvider() {
    return provider;
  }
}
