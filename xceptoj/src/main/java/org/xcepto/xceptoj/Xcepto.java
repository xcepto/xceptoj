package org.xcepto.xceptoj;

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
    try{
      scenario.startEnvironment();

      var transitionBuilder = new TransitionBuilder();
      builder.accept(transitionBuilder);

      AcceptanceTest runner = new AcceptanceTest(transitionBuilder.getAdapters());
      runner.executeTest(transitionBuilder.build(), timeout, checkInterval);

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