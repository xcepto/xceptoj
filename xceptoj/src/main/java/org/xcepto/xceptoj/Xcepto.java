package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;

import java.time.Duration;
import java.util.function.Consumer;

public class Xcepto {
  public static <TScenario extends Scenario> void Given(
      TScenario scenario, Consumer<TransitionBuilder> builder) throws
      XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    Given(scenario, builder, Duration.ofSeconds(10), Duration.ofMillis(100));
  }

  public static <TScenario extends Scenario> void Given(
      TScenario scenario,
      Consumer<TransitionBuilder> builder,
      Duration timeout,
      Duration checkInterval)
      throws XceptoAdapterInitializationException,
      XceptoTestFailedException, XceptoScenarioResetException, XceptoAdapterTerminationException {
    try{
      scenario.startEnvironment();

      var transitionBuilder = new TransitionBuilder();
      builder.accept(transitionBuilder);

      AcceptanceTest runner = new AcceptanceTest(transitionBuilder.GetAdapters());
      runner.ExecuteTest(transitionBuilder.Build(), timeout, checkInterval);

      scenario.stopEnvironment();
    } catch (XceptoAdapterInitializationException|
      XceptoTestFailedException|
      XceptoScenarioResetException|
      XceptoAdapterTerminationException e) {
      scenario.stopEnvironment();
      throw e;
    }
  }
}