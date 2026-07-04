package org.xcepto.xceptoj.exceptionpropagation.scenarios;

import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;

import java.util.concurrent.CompletableFuture;

public class FireAndForgetScenario extends Scenario {
  @Override
  public void initializeEnvironment() {
    fireAndForget(CompletableFuture.failedFuture(new PropagatedException()));
  }

  @Override
  public void stopEnvironment() {
  }
}
