package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.AdapterCleanupException;
import org.xcepto.xceptoj.exceptions.AdapterInitException;
import org.xcepto.xceptoj.exceptions.TestTimeoutException;
import org.xcepto.xceptoj.exceptions.TotalTimeoutException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AcceptanceTest {

  private final Iterable<XceptoAdapter> adapters;
  private final List<CompletableFuture<?>> propagatedFutures;

  public AcceptanceTest(Iterable<XceptoAdapter> adapters, List<CompletableFuture<?>> propagatedFutures) {
    this.adapters = adapters;
    this.propagatedFutures = propagatedFutures;
  }

  protected void executeTest(XceptoStateMachine stateMachine,
                             TimeoutConfig timeout,
                             Duration checkInterval,
                             LoggingProvider loggingProvider,
                             Instant totalStart)
      throws XceptoTestFailedException, XceptoAdapterInitializationException, XceptoAdapterTerminationException {

    var serviceCollection = new org.xcepto.xceptoj.di.ServiceProvider();
    for (XceptoAdapter adapter : adapters) {
      adapter.addServices(serviceCollection);
    }
    serviceCollection.register(LoggingProvider.class, () -> loggingProvider);
    ServiceProvider serviceProvider = serviceCollection.buildServiceProvider();

    var adaptersToTerminate = new ArrayList<XceptoAdapter>();
    Throwable primaryFailure = null;

    try {
      for (XceptoAdapter adapter : adapters) {
        try {
          adaptersToTerminate.add(adapter);
          adapter.initialize(serviceProvider);
        } catch (XceptoAdapterInitializationException e) {
          primaryFailure = e;
          throw e;
        } catch (Exception e) {
          var wrapped = new AdapterInitException("Adapter initialization failed", e);
          primaryFailure = wrapped;
          throw wrapped;
        }
      }

      stateMachine.initialize(serviceProvider);

      Instant testStart = Instant.now();
      stateMachine.start(serviceProvider);

      while (true) {
        Instant now = Instant.now();
        if (Duration.between(testStart, now).compareTo(timeout.getTest()) >= 0) {
          throw new TestTimeoutException(
              "Current state was %s, expected was %s".formatted(
                  stateMachine.getCurrentState(), stateMachine.getFinalState()));
        }
        if (Duration.between(totalStart, now).compareTo(timeout.getTotal()) >= 0) {
          throw new TotalTimeoutException(
              "Test exceeded TOTAL timeout: %s during [%s]".formatted(
                  timeout.getTotal(), stateMachine.getCurrentState()));
        }

        stateMachine.tryTransition(serviceProvider);

        checkPropagated();

        if (stateMachine.getCurrentState().equals(stateMachine.getFinalState())) {
          break;
        }

        loggingProvider.flush();

        try {
          Thread.sleep(checkInterval);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new XceptoTestFailedException("Test execution was interrupted", e);
        }
      }
    } catch (XceptoTestFailedException e) {
      primaryFailure = e;
      throw e;
    } finally {
      terminateAdapters(adaptersToTerminate, serviceProvider, primaryFailure);
    }
  }

  private void checkPropagated() throws XceptoTestFailedException {
    for (CompletableFuture<?> future : propagatedFutures) {
      if (future.isCompletedExceptionally()) {
        try {
          future.get();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause() != null ? e.getCause() : e;
          if (cause instanceof XceptoTestFailedException xef) {
            throw xef;
          }
          if (cause instanceof RuntimeException re) {
            throw re;
          }
          throw new RuntimeException(cause);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new XceptoTestFailedException("Propagated task check interrupted", e);
        }
      }
    }
  }

  private void terminateAdapters(Iterable<XceptoAdapter> adaptersToTerminate,
                                  ServiceProvider serviceProvider, Throwable primaryFailure)
      throws XceptoAdapterTerminationException {
    XceptoAdapterTerminationException cleanupFailure = null;
    for (XceptoAdapter adapter : adaptersToTerminate) {
      try {
        adapter.terminate(serviceProvider);
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
