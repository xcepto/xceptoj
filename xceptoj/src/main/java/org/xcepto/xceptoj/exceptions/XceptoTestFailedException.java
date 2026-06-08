package org.xcepto.xceptoj.exceptions;

public class XceptoTestFailedException extends Exception {
  public XceptoTestFailedException(String message) {
    super(message);
  }

  public XceptoTestFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
