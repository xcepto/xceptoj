package org.xcepto.xceptoj.cleanupexecution;

public class CleanupFailure extends RuntimeException {
  public CleanupFailure() {
    super("cleanup execution failure");
  }
}
