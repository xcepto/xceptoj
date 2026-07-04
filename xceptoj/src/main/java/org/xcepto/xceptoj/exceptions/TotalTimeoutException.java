package org.xcepto.xceptoj.exceptions;

public class TotalTimeoutException extends XceptoTestFailedException {
  public TotalTimeoutException(String message) {
    super(message);
  }

  public TotalTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
