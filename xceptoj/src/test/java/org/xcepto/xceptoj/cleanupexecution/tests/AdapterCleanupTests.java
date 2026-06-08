package org.xcepto.xceptoj.cleanupexecution.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.cleanupexecution.adapters.FailingInitAdapter;
import org.xcepto.xceptoj.cleanupexecution.adapters.SuccessfulAdapter;
import org.xcepto.xceptoj.cleanupexecution.scenarios.TrackableCleanupScenario;
import org.xcepto.xceptoj.exceptions.AdapterInitException;

class AdapterCleanupTests {
  @Test
  void successfulAdapterCleanedUp() throws Exception {
    var adapter = new SuccessfulAdapter();

    Xcepto.given(new TrackableCleanupScenario(), builder -> builder.registerAdapter(adapter));

    assertTrue(adapter.cleanedUp());
  }

  @Test
  void failingInitAdapterCleanedUp() {
    var adapter = new FailingInitAdapter();

    assertThrows(AdapterInitException.class,
        () -> Xcepto.given(new TrackableCleanupScenario(), builder -> builder.registerAdapter(adapter)));

    assertTrue(adapter.cleanedUp());
  }
}
