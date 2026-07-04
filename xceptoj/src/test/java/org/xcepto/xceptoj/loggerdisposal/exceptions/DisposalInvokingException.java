package org.xcepto.xceptoj.loggerdisposal.exceptions;

public class DisposalInvokingException extends RuntimeException {
  public DisposalInvokingException() {
    super("Exception that triggers disposal-path verification");
  }
}
