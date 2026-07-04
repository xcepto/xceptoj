package org.xcepto.xceptoj.exceptionpropagation.exceptions;

public class PropagatedException extends RuntimeException {
  public PropagatedException() {
    super("Propagated background failure");
  }
}
