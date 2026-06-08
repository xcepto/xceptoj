package org.xcepto.xceptoj;

import org.xcepto.xceptoj.exceptions.StateEnterException;
import org.xcepto.xceptoj.exceptions.StateInitException;
import org.xcepto.xceptoj.exceptions.StateTransitionException;
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

  public void initialize(ServiceProvider serviceProvider) throws StateInitException {
    var state = startState;
    while (state != null) {
      try {
        state.initialize(serviceProvider);
      } catch (StateInitException e) {
        throw e;
      } catch (Exception e) {
        throw new StateInitException("State initialization failed for %s".formatted(state.getName()), e);
      }
      state = state.getNextState();
    }
  }

  public void start(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    enterCurrentState(serviceProvider);
  }

  public void tryTransition(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    if (currentState.getNextState() == null)
      return;

    boolean allConditionsMet;
    try {
      allConditionsMet = currentState.evaluateConditionsForTransition(serviceProvider);
    } catch (StateTransitionException e) {
      throw e;
    } catch (Exception e) {
      throw new StateTransitionException("State transition failed for %s".formatted(currentState.getName()), e);
    }
    if (allConditionsMet) {
      currentState = currentState.getNextState();
      enterCurrentState(serviceProvider);
      var loggingProvider = serviceProvider.get(LoggingProvider.class);
      loggingProvider.logDebug("Transitioned to: " + currentState.getName());
    }
  }

  private void enterCurrentState(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    try {
      currentState.onEnter(serviceProvider);
    } catch (StateEnterException e) {
      throw e;
    } catch (Exception e) {
      throw new StateEnterException("State entry failed for %s".formatted(currentState.getName()), e);
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
