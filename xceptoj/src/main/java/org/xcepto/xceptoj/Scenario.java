package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Scenario {

  private boolean _setupComplete = false;
  private boolean _initializedComplete = false;
  private final List<CompletableFuture<?>> _fireAndForgetFutures = new ArrayList<>();

  public void setupEnvironment() throws XceptoScenarioResetException {
  }

  public void initializeEnvironment() throws XceptoScenarioResetException {
    startEnvironment();
  }

  public void startEnvironment() throws XceptoScenarioResetException {
  }

  public abstract void stopEnvironment() throws XceptoScenarioResetException;

  public int getPort(String service, int port) {
    return -1;
  }

  public LoggingProvider createLoggingProvider() {
    return new org.xcepto.xceptoj.LoggingProvider();
  }

  protected void fireAndForget(CompletableFuture<?> future) {
    _fireAndForgetFutures.add(future);
  }

  List<CompletableFuture<?>> getFireAndForgetFutures() {
    return _fireAndForgetFutures;
  }

  boolean isSetupComplete() {
    return _setupComplete;
  }

  void markSetupComplete() {
    _setupComplete = true;
  }

  boolean isInitializedComplete() {
    return _initializedComplete;
  }

  void markInitializedComplete() {
    _initializedComplete = true;
  }

  public void teardown() throws XceptoScenarioResetException {
    _setupComplete = false;
    _initializedComplete = false;
    _fireAndForgetFutures.clear();
    stopEnvironment();
  }
}
