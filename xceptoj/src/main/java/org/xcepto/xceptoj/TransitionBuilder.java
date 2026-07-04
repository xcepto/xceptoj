package org.xcepto.xceptoj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TransitionBuilder {
  private final XceptoStateMachine _stateMachine = new XceptoStateMachine();
  private final HashSet<XceptoAdapter> _adapters = new HashSet<>();
  private final List<CompletableFuture<?>> _propagatedFutures = new ArrayList<>();
  private final LinkedHashMap<Object, Supplier<XceptoState>> _pendingSteps = new LinkedHashMap<>();

  public void addStep(XceptoState newState) {
    _pendingSteps.put(new Object(), () -> newState);
  }

  public void addFutureStep(Supplier<XceptoState> stateFactory, Object key) {
    _pendingSteps.put(key, stateFactory);
  }

  XceptoStateMachine build() {
    for (var entry : _pendingSteps.entrySet()) {
      XceptoState state = entry.getValue().get();
      state.assignBuilder(this);
      _stateMachine.addTransition(state);
    }
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