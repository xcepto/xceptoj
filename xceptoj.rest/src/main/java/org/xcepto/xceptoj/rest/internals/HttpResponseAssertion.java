package org.xcepto.xceptoj.rest.internals;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;

import java.net.http.HttpResponse;

@FunctionalInterface
public interface HttpResponseAssertion {
  void check(HttpResponse<String> response) throws XceptoTestFailedException;
}
