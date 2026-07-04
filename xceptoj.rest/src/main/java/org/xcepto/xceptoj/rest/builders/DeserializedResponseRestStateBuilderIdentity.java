package org.xcepto.xceptoj.rest.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rest.Promise;
import org.xcepto.xceptoj.rest.Serializer;
import org.xcepto.xceptoj.rest.internals.RestHttpState;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class DeserializedResponseRestStateBuilderIdentity<TResponse>
    extends AbstractRestStateBuilderIdentity<DeserializedResponseRestStateBuilderIdentity<TResponse>> {

  private final Class<TResponse> responseType;
  private Promise<TResponse> promise = null;

  DeserializedResponseRestStateBuilderIdentity(TransitionBuilder tb, Object key,
      AbstractRestStateBuilderIdentity<?> copyFrom, Class<TResponse> type) {
    super(tb, key, copyFrom);
    this.responseType = type;
  }

  @Override
  protected DeserializedResponseRestStateBuilderIdentity<TResponse> self() {
    return this;
  }

  public DeserializedResponseRestStateBuilderIdentity<TResponse> assertThatResponse(Predicate<TResponse> predicate) {
    return assertThatResponse(response -> {
      if (serializer == null)
        throw new XceptoTestFailedException(
            "No serializer defined; call withSerializer() before assertThatResponse(Predicate)");
      TResponse deserialized = serializer.deserialize(response.body(), responseType);
      if (!predicate.test(deserialized))
        throw new XceptoTestFailedException(
            "Response assertion failed for %s. Body was: %s".formatted(responseType.getSimpleName(), response.body()));
    });
  }

  public Promise<TResponse> promiseResponse() {
    promise = new Promise<>();
    return promise;
  }

  @Override
  protected RestHttpState buildState() {
    Promise<TResponse> capturedPromise = this.promise;
    Class<TResponse> capturedType = this.responseType;
    Serializer capturedSerializer = this.serializer;

    return new RestHttpState(
        resolveName(),
        buildUrlProducer(),
        clientProducer,
        bearerToken,
        verb,
        resolveRetry(),
        requestBody,
        new ArrayList<>(assertions),
        response -> {
          if (capturedPromise != null) {
            if (capturedSerializer == null)
              throw new XceptoTestFailedException(
                  "No serializer defined; call withSerializer() to use promiseResponse()");
            TResponse deserialized = capturedSerializer.deserialize(response.body(), capturedType);
            capturedPromise.settle(deserialized);
          }
        });
  }
}
