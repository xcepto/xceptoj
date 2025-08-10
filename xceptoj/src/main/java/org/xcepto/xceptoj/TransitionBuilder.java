package org.xcepto.xceptoj;

import java.util.HashSet;

public class TransitionBuilder {
  private final XceptoStateMachine _stateMachine = new XceptoStateMachine();
  private final HashSet<XceptoAdapter> _adapters = new HashSet<>();

  public void addStep(XceptoState newState) {
    _stateMachine.addTransition(newState);
  }

  XceptoStateMachine build() {
    _stateMachine.seal();
    return _stateMachine;
  }

  Iterable<XceptoAdapter> getAdapters() {
    return _adapters;
  }

  public <TXceptoAdapter extends XceptoAdapter> TXceptoAdapter registerAdapter(TXceptoAdapter adapter) {
    adapter.injectBuilder(this);
    _adapters.add(adapter);
    return adapter;
  }
}