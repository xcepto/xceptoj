package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

public class XceptoStateMachine {
  private final XceptoState startState;
  private final XceptoState finalState;
  private XceptoState currentState;

  public XceptoStateMachine() {
    startState = new UnconditionalXceptoState("Start");
    finalState = new UnconditionalXceptoState("Final");
    currentState = startState;
  }

  public void start(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    currentState.onEnter(serviceProvider);
  }

  public void tryTransition(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    if (currentState.getNextState() == null)
      return;

    var allConditionsMet = currentState.evaluateConditionsForTransition(serviceProvider);
    if (allConditionsMet) {
      currentState = currentState.getNextState();
      currentState.onEnter(serviceProvider);
      var loggingProvider = serviceProvider.get(LoggingProvider.class);
      loggingProvider.logDebug("Transitioned to: " + currentState.getName());
    }
  }

  public XceptoState getCurrentState() {
    return currentState;
  }

  public XceptoState getFinalState() {
    return finalState;
  }

  public void seal() {
    currentState.setNextState(finalState);
    currentState = startState;
  }

  public void addTransition(XceptoState state) {
    currentState.setNextState(state);
    currentState = state;
  }
}
