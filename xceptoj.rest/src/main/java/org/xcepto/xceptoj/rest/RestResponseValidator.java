package org.xcepto.xceptoj.rest;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;

@FunctionalInterface
public interface RestResponseValidator {
  boolean test(Object t) throws XceptoTestFailedException;
}
