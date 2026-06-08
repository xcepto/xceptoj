package org.xcepto.xceptoj.exceptiondetail.tests;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ExceptionAssertions {
  private ExceptionAssertions() {
  }

  static <T extends Throwable> T assertStageFailure(
      Class<T> expectedStage,
      Class<? extends Throwable> expectedCause,
      ThrowingRunnable action) {
    T thrown = assertThrows(expectedStage, action::run);
    assertNotNull(thrown.getCause());
    assertInstanceOf(expectedCause, thrown.getCause());
    return thrown;
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }
}
