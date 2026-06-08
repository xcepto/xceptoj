package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.AdapterCleanupException;
import org.xcepto.xceptoj.exceptions.AdapterInitException;
import org.xcepto.xceptoj.exceptions.TestTimeoutException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;

public class AcceptanceTest {

  private final Iterable<XceptoAdapter> adapters;

  public AcceptanceTest(Iterable<XceptoAdapter> adapters) {
    this.adapters = adapters;
  }

  protected void executeTest(XceptoStateMachine stateMachine,
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

    var initializedAdapters = new ArrayList<XceptoAdapter>();
    Throwable primaryFailure = null;

    try {
      for (XceptoAdapter adapter : adapters) {
        try {
          adapter.initialize(serviceProvider);
          initializedAdapters.add(adapter);
        } catch (XceptoAdapterInitializationException e) {
          primaryFailure = e;
          throw e;
        } catch (Exception e) {
          var wrapped = new AdapterInitException("Adapter initialization failed", e);
          primaryFailure = wrapped;
          throw wrapped;
        }
      }

      // Act
      stateMachine.initialize(serviceProvider);
      Instant startTime = Instant.now();
      stateMachine.start(serviceProvider);
      Duration elapsed = Duration.between(startTime, Instant.now());
      while (elapsed.toMillis() < timeout.toMillis()) {
        stateMachine.tryTransition(serviceProvider);
        if (stateMachine.getCurrentState().equals(stateMachine.getFinalState()))
          break;
        try {
          Thread.sleep(checkInterval);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new XceptoTestFailedException("Test execution was interrupted", e);
        }
        elapsed = Duration.between(startTime, Instant.now());
      }

      // Assert
      if (!stateMachine.getCurrentState().equals(stateMachine.getFinalState())) {
        throw new TestTimeoutException("Current state was %s, expected was %s"
            .formatted(stateMachine.getCurrentState(), stateMachine.getFinalState()));
      }
    } catch (XceptoTestFailedException e) {
      primaryFailure = e;
      throw e;
    } finally {
      terminateInitializedAdapters(initializedAdapters, primaryFailure);
    }
  }

  private void terminateInitializedAdapters(Iterable<XceptoAdapter> initializedAdapters, Throwable primaryFailure)
      throws XceptoAdapterTerminationException {
    XceptoAdapterTerminationException cleanupFailure = null;
    for (XceptoAdapter adapter : initializedAdapters) {
      try {
        adapter.terminate();
      } catch (XceptoAdapterTerminationException e) {
        cleanupFailure = e;
        break;
      } catch (Exception e) {
        cleanupFailure = new AdapterCleanupException("Adapter cleanup failed", e);
        break;
      }
    }

    if (cleanupFailure != null) {
      if (primaryFailure != null) {
        primaryFailure.addSuppressed(cleanupFailure);
        return;
      }
      throw cleanupFailure;
    }
  }
}
