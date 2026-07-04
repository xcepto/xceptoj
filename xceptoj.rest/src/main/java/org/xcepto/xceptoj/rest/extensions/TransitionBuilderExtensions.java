package org.xcepto.xceptoj.rest.extensions;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;

public final class TransitionBuilderExtensions {
  private TransitionBuilderExtensions() {}

  public static RestAdapterBuilder restAdapterBuilder(TransitionBuilder builder) {
    return new RestAdapterBuilder(builder);
  }
}
