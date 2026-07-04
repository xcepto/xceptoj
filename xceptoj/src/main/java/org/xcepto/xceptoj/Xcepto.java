package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.ArrangeTestException;
import org.xcepto.xceptoj.exceptions.ScenarioCleanupException;
import org.xcepto.xceptoj.exceptions.ScenarioInitException;
import org.xcepto.xceptoj.exceptions.ScenarioSetupException;
import org.xcepto.xceptoj.exceptions.TotalTimeoutException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class Xcepto {

  private static final TimeoutConfig DEFAULT_TIMEOUT = TimeoutConfig.fromSeconds(10);
  private static final Duration DEFAULT_CHECK_INTERVAL = Duration.ofMillis(100);

  public static <TScenario extends Scenario> void given(
      TScenario scenario, Consumer<TransitionBuilder> builder) throws
      XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    given(scenario, builder, DEFAULT_TIMEOUT, DEFAULT_CHECK_INTERVAL);
  }

  public static <TScenario extends Scenario> void given(
      TScenario scenario,
      Consumer<TransitionBuilder> builder,
      Duration timeout,
      Duration checkInterval)
      throws XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    given(scenario, builder, new TimeoutConfig(timeout, timeout), checkInterval);
  }

  public static <TScenario extends Scenario> void given(
      TScenario scenario,
      Consumer<TransitionBuilder> builder,
      TimeoutConfig timeout,
      Duration checkInterval)
      throws XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {

    Instant totalStart = Instant.now();
    Throwable primaryFailure = null;
    LoggingProvider loggingProvider = scenario.createLoggingProvider();

    try {
      if (!scenario.isSetupComplete()) {
        try {
          runWithDeadline(scenario::setupEnvironment, totalStart, timeout.getTotal(), "scenario setup");
        } catch (XceptoScenarioResetException e) {
          primaryFailure = e;
          throw e;
        } catch (TotalTimeoutException e) {
          primaryFailure = e;
          throw e;
        } catch (Exception e) {
          var wrapped = new ScenarioSetupException("Scenario setup failed", e);
          primaryFailure = wrapped;
          throw wrapped;
        }
        scenario.markSetupComplete();
      }

      if (!scenario.isInitializedComplete()) {
        try {
          runWithDeadline(scenario::initializeEnvironment, totalStart, timeout.getTotal(), "scenario initialization");
        } catch (XceptoScenarioResetException e) {
          primaryFailure = e;
          throw e;
        } catch (TotalTimeoutException e) {
          primaryFailure = e;
          throw e;
        } catch (Exception e) {
          var wrapped = new ScenarioInitException("Scenario initialization failed", e);
          primaryFailure = wrapped;
          throw wrapped;
        }
        scenario.markInitializedComplete();
      }

      var transitionBuilder = arrange(builder);

      for (var future : scenario.getFireAndForgetFutures()) {
        transitionBuilder.propagateExceptions(future);
      }

      AcceptanceTest runner = new AcceptanceTest(
          transitionBuilder.getAdapters(),
          transitionBuilder.getPropagatedFutures());
      runner.executeTest(transitionBuilder.build(), timeout, checkInterval, loggingProvider, totalStart);
    } catch (XceptoAdapterInitializationException |
             XceptoTestFailedException |
             XceptoScenarioResetException |
             XceptoAdapterTerminationException e) {
      primaryFailure = e;
      throw e;
    } finally {
      loggingProvider.flush();
      try {
        loggingProvider.close();
      } catch (Exception ignored) {
      }
      stopScenario(scenario, primaryFailure);
    }
  }

  private static TransitionBuilder arrange(Consumer<TransitionBuilder> builder) throws ArrangeTestException {
    try {
      var transitionBuilder = new TransitionBuilder();
      builder.accept(transitionBuilder);
      return transitionBuilder;
    } catch (Exception e) {
      throw new ArrangeTestException("Test arrangement failed", e);
    }
  }

  private static void stopScenario(Scenario scenario, Throwable primaryFailure) throws XceptoScenarioResetException {
    try {
      scenario.stopEnvironment();
    } catch (XceptoScenarioResetException e) {
      if (primaryFailure != null) {
        primaryFailure.addSuppressed(e);
        return;
      }
      throw e;
    } catch (Exception e) {
      var cleanupFailure = new ScenarioCleanupException("Scenario cleanup failed", e);
      if (primaryFailure != null) {
        primaryFailure.addSuppressed(cleanupFailure);
        return;
      }
      throw cleanupFailure;
    }
  }

  @FunctionalInterface
  interface ScenarioAction {
    void run() throws XceptoScenarioResetException;
  }

  static void runWithDeadline(ScenarioAction action, Instant totalStart, Duration totalBudget, String phase)
      throws XceptoScenarioResetException, TotalTimeoutException {
    Duration elapsed = Duration.between(totalStart, Instant.now());
    Duration remaining = totalBudget.minus(elapsed);
    if (!remaining.isPositive()) {
      throw new TotalTimeoutException("Test exceeded TOTAL timeout before " + phase);
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<?> future = executor.submit(() -> {
      try {
        action.run();
      } catch (XceptoScenarioResetException e) {
        throw new RuntimeException(e);
      }
    });

    try {
      future.get(remaining.toMillis(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new TotalTimeoutException("Test exceeded TOTAL timeout during " + phase);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException re && re.getCause() instanceof XceptoScenarioResetException xsre) {
        throw xsre;
      }
      if (cause instanceof XceptoScenarioResetException xsre) {
        throw xsre;
      }
      if (cause instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(cause);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TotalTimeoutException("Test was interrupted during " + phase);
    } finally {
      executor.shutdownNow();
    }
  }
}
