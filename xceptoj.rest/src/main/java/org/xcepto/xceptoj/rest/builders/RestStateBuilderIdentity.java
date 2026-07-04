package org.xcepto.xceptoj.rest.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.rest.internals.RestHttpState;

import java.util.ArrayList;

public final class RestStateBuilderIdentity extends AbstractRestStateBuilderIdentity<RestStateBuilderIdentity> {

  public RestStateBuilderIdentity(TransitionBuilder tb) {
    super(tb);
  }

  @Override
  protected RestStateBuilderIdentity self() {
    return this;
  }

  @Override
  protected RestHttpState buildState() {
    return new RestHttpState(
        resolveName(),
        buildUrlProducer(),
        clientProducer,
        bearerToken,
        verb,
        resolveRetry(),
        requestBody,
        new ArrayList<>(assertions),
        response -> {});
  }
}
