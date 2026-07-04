package org.xcepto.xceptoj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransitionBuilder {
  private final XceptoStateMachine _stateMachine = new XceptoStateMachine();
  private final HashSet<XceptoAdapter> _adapters = new HashSet<>();
  private final List<CompletableFuture<?>> _propagatedFutures = new ArrayList<>();

  public void addStep(XceptoState newState) {
    newState.assignBuilder(this);
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

  public void propagateExceptions(CompletableFuture<?> future) {
    _propagatedFutures.add(future);
  }

  List<CompletableFuture<?>> getPropagatedFutures() {
    return _propagatedFutures;
  }
}