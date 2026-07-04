package org.xcepto.xceptoj.ssr.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.http.Promise;
import org.xcepto.xceptoj.http.builders.AbstractHttpStateBuilderIdentity;
import org.xcepto.xceptoj.ssr.internals.SsrHttpState;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

public final class SsrStateBuilderIdentity extends AbstractHttpStateBuilderIdentity<SsrStateBuilderIdentity> {

  private Supplier<Map<String, String>> formContent = null;
  private Promise<String> promise = null;

  public SsrStateBuilderIdentity(TransitionBuilder tb) {
    super(tb);
  }

  @Override
  protected SsrStateBuilderIdentity self() {
    return this;
  }

  @Override
  protected String adapterPrefix() { return "SSR"; }

  public SsrStateBuilderIdentity withFormContent(Map<String, String> form) {
    this.formContent = () -> form;
    return this;
  }

  public SsrStateBuilderIdentity withFormContent(Supplier<Map<String, String>> formProducer) {
    this.formContent = formProducer;
    return this;
  }

  public Promise<String> promiseResponse() {
    promise = new Promise<>();
    return promise;
  }

  @Override
  protected SsrHttpState buildState() {
    Promise<String> capturedPromise = this.promise;
    return new SsrHttpState(
        resolveName(),
        buildUrlProducer(),
        clientProducer,
        verb,
        resolveRetry(),
        formContent,
        new ArrayList<>(assertions),
        response -> {
          if (capturedPromise != null) {
            capturedPromise.settle(response.body());
          }
        });
  }
}
