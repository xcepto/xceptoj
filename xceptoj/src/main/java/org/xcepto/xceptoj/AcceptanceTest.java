package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.time.Duration;
import java.time.Instant;

public class AcceptanceTest {

  private final Iterable<XceptoAdapter> adapters;

  public AcceptanceTest(Iterable<XceptoAdapter> adapters) {
    this.adapters = adapters;
  }

  protected void ExecuteTest(XceptoStateMachine stateMachine,
                             Duration timeout,
                             Duration checkInterval)
      throws XceptoTestFailedException, XceptoAdapterInitializationException, XceptoAdapterTerminationException {


    // Arrange
    var serviceCollection = new org.xcepto.xceptoj.di.ServiceProvider();
    for (XceptoAdapter adapter : adapters) {
      adapter.addServices(serviceCollection);
    }
    serviceCollection.register(LoggingProvider.class, org.xcepto.xceptoj.LoggingProvider::new);
    ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();

    for (XceptoAdapter adapter : adapters) {
      adapter.initialize(serviceProvider);
    }

    // Act
    Instant startTime = Instant.now();
    stateMachine.Start(serviceProvider);
    Duration elapsed = Duration.between(startTime, Instant.now());
    while (elapsed.toMillis() < timeout.toMillis()) {
      stateMachine.TryTransition(serviceProvider);
      if (stateMachine.getCurrentState().equals(stateMachine.getFinalState()))
        break;
      try {
        Thread.sleep(checkInterval);
      } catch (InterruptedException e) {
        throw new XceptoTestFailedException(e.getCause().toString());
      }
      elapsed = Duration.between(startTime, Instant.now());
    }

    // Assert
    if (!stateMachine.getCurrentState().equals(stateMachine.getFinalState())) {
      throw new XceptoTestFailedException("Current state was %s, expected was %s"
          .formatted(stateMachine.getCurrentState(), stateMachine.getFinalState()));
    }

    for (XceptoAdapter adapter : adapters) {
      adapter.terminate();
    }
  }
}
