package org.xcepto.xceptoj.ssr.extensions;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;

public final class TransitionBuilderExtensions {

  private TransitionBuilderExtensions() {}

  public static SsrAdapterBuilder ssrAdapterBuilder(TransitionBuilder builder) {
    return new SsrAdapterBuilder(builder);
  }
}
