package org.xcepto.xceptoj.exceptionpropagation.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptionpropagation.adapters.PropagatedAdapter;
import org.xcepto.xceptoj.exceptionpropagation.adapters.UnpropagatedAdapter;
import org.xcepto.xceptoj.exceptionpropagation.exceptions.PropagatedException;
import org.xcepto.xceptoj.exceptionpropagation.scenarios.SimpleSyncScenario;

class AdapterExceptionPropagationTests {

  @Test
  void propagatedAdapterFutureFailsTest() {
    assertThrows(PropagatedException.class,
        () -> Xcepto.given(
            new SimpleSyncScenario(),
            builder -> builder.registerAdapter(new PropagatedAdapter())));
  }

  @Test
  void unpropagatedAdapterFutureDoesNotFailTest() {
    assertDoesNotThrow(() ->
        Xcepto.given(
            new SimpleSyncScenario(),
            builder -> builder.registerAdapter(new UnpropagatedAdapter())));
  }
}
