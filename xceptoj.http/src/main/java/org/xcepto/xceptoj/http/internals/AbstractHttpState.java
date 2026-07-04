package org.xcepto.xceptoj.http.internals;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.data.HttpResponseAssertion;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.net.http.HttpResponse;
import java.util.List;

public abstract class AbstractHttpState extends XceptoState {

  @FunctionalInterface
  public interface ResponseAction {
    void accept(HttpResponse<String> response) throws XceptoTestFailedException;
  }

  private final List<HttpResponseAssertion> assertions;
  private final boolean retry;
  private final ResponseAction responseAction;

  protected AbstractHttpState(String name,
                               List<HttpResponseAssertion> assertions,
                               boolean retry,
                               ResponseAction responseAction) {
    super(name);
    this.assertions = assertions;
    this.retry = retry;
    this.responseAction = responseAction;
  }

  @Override
  public void onEnter(ServiceProvider sp) throws XceptoTestFailedException {
    if (!retry) {
      HttpResponse<String> response = executeRequest(sp);
      checkAssertions(response, false);
      responseAction.accept(response);
    }
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider sp) throws XceptoTestFailedException {
    if (!retry) return true;
    HttpResponse<String> response = executeRequest(sp);
    if (checkAssertions(response, true)) {
      responseAction.accept(response);
      return true;
    }
    return false;
  }

  private boolean checkAssertions(HttpResponse<String> response, boolean softFail) throws XceptoTestFailedException {
    for (HttpResponseAssertion assertion : assertions) {
      try {
        assertion.check(response);
      } catch (XceptoTestFailedException e) {
        if (softFail) return false;
        throw e;
      }
    }
    return true;
  }

  protected abstract HttpResponse<String> executeRequest(ServiceProvider sp) throws XceptoTestFailedException;
}
