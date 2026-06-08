package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.ArrangeTestException;
import org.xcepto.xceptoj.exceptions.ScenarioCleanupException;
import org.xcepto.xceptoj.exceptions.ScenarioInitException;
import org.xcepto.xceptoj.exceptions.ScenarioSetupException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;

import java.time.Duration;
import java.util.function.Consumer;

public class Xcepto {
  public static <TScenario extends Scenario> void given(
      TScenario scenario, Consumer<TransitionBuilder> builder) throws
      XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    given(scenario, builder, Duration.ofSeconds(10), Duration.ofMillis(100));
  }

  public static <TScenario extends Scenario> void given(
      TScenario scenario,
      Consumer<TransitionBuilder> builder,
      Duration timeout,
      Duration checkInterval)
      throws XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    Throwable primaryFailure = null;

    try {
      try {
        scenario.setupEnvironment();
      } catch (XceptoScenarioResetException e) {
        primaryFailure = e;
        throw e;
      } catch (Exception e) {
        var wrapped = new ScenarioSetupException("Scenario setup failed", e);
        primaryFailure = wrapped;
        throw wrapped;
      }

      try {
        scenario.initializeEnvironment();
      } catch (XceptoScenarioResetException e) {
        primaryFailure = e;
        throw e;
      } catch (Exception e) {
        var wrapped = new ScenarioInitException("Scenario initialization failed", e);
        primaryFailure = wrapped;
        throw wrapped;
      }

      var transitionBuilder = arrange(builder);

      AcceptanceTest runner = new AcceptanceTest(transitionBuilder.getAdapters());
      runner.executeTest(transitionBuilder.build(), timeout, checkInterval);
    } catch (XceptoAdapterInitializationException|
      XceptoTestFailedException|
      XceptoScenarioResetException|
      XceptoAdapterTerminationException e) {
      primaryFailure = e;
      throw e;
    } finally {
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
}
