package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

public abstract class Scenario {

  public abstract void startEnvironment() throws XceptoScenarioResetException;
  public abstract void stopEnvironment() throws XceptoScenarioResetException;
  public abstract int getPort(String service, int port);
}
