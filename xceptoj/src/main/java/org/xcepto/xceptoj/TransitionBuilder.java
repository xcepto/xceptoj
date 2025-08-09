package org.xcepto.xceptoj;

import java.util.HashSet;

public class TransitionBuilder {
  private final XceptoStateMachine _stateMachine = new XceptoStateMachine();
  private final HashSet<XceptoAdapter> _adapters = new HashSet<>();

  public void AddStep(XceptoState newState) {
    _stateMachine.AddTransition(newState);
  }

  XceptoStateMachine Build() {
    _stateMachine.Seal();
    return _stateMachine;
  }

  Iterable<XceptoAdapter> GetAdapters() {
    return _adapters;
  }

  public <TXceptoAdapter extends XceptoAdapter> TXceptoAdapter RegisterAdapter(TXceptoAdapter adapter) {
    adapter.injectBuilder(this);
    _adapters.add(adapter);
    return adapter;
  }
}