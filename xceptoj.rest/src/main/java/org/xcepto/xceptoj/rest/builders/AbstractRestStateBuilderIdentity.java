package org.xcepto.xceptoj.rest.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.builders.AbstractHttpStateBuilderIdentity;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.http.Promise;
import org.xcepto.xceptoj.rest.Serializer;
import org.xcepto.xceptoj.rest.data.RequestBody;
import org.xcepto.xceptoj.rest.internals.RestHttpState;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractRestStateBuilderIdentity<T extends AbstractRestStateBuilderIdentity<T>>
    extends AbstractHttpStateBuilderIdentity<T> {

  protected RequestBody requestBody = null;
  protected Serializer serializer = null;
  protected Supplier<String> bearerToken = null;

  protected AbstractRestStateBuilderIdentity(TransitionBuilder tb) {
    super(tb);
  }

  protected AbstractRestStateBuilderIdentity(TransitionBuilder tb, Object key,
      AbstractRestStateBuilderIdentity<?> copyFrom) {
    super(tb, key, copyFrom);
    this.requestBody = copyFrom.requestBody;
    this.serializer = copyFrom.serializer;
    this.bearerToken = copyFrom.bearerToken;
  }

  @Override
  protected String adapterPrefix() { return "REST"; }

  public T withSerializer(Serializer serializer) {
    this.serializer = serializer;
    return self();
  }

  public <TRequest> T withRequestBody(Supplier<TRequest> bodySupplier) {
    Serializer s = this.serializer;
    this.requestBody = new RequestBody(
        () -> bodySupplier.get(),
        obj -> {
          if (s == null)
            throw new IllegalStateException(
                "No serializer defined; call withSerializer() or use withRequestBody(supplier, serializationFn)");
          return s.serialize(obj);
        });
    return self();
  }

  public <TRequest> T withRequestBody(Supplier<TRequest> bodySupplier, Function<TRequest, String> customSerialization) {
    @SuppressWarnings("unchecked")
    Function<Object, String> fn = obj -> customSerialization.apply((TRequest) obj);
    this.requestBody = new RequestBody(() -> bodySupplier.get(), fn);
    return self();
  }

  public T withBearerTokenClient(Supplier<String> tokenSelector) {
    this.bearerToken = tokenSelector;
    return self();
  }

  public <TResponse> DeserializedResponseRestStateBuilderIdentity<TResponse> withResponseType(Class<TResponse> type) {
    return new DeserializedResponseRestStateBuilderIdentity<>(transitionBuilder, identityKey, this, type);
  }

}
