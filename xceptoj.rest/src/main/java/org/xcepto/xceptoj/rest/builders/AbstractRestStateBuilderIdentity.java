package org.xcepto.xceptoj.rest.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rest.Serializer;
import org.xcepto.xceptoj.rest.data.HttpMethodVerb;
import org.xcepto.xceptoj.rest.data.RequestBody;
import org.xcepto.xceptoj.rest.internals.HttpResponseAssertion;
import org.xcepto.xceptoj.rest.internals.RestHttpState;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.net.URLEncoder;

public abstract class AbstractRestStateBuilderIdentity<T extends AbstractRestStateBuilderIdentity<T>> {
  protected final TransitionBuilder transitionBuilder;
  protected final Object identityKey;

  protected Supplier<HttpClient> clientProducer = HttpClient::newHttpClient;
  protected Supplier<URI> baseUrlProducer = () -> URI.create("http://localhost:8080");
  protected HttpMethodVerb verb = HttpMethodVerb.GET;
  protected Supplier<String> pathProducer = () -> "/";
  protected final List<Supplier<String[]>> queryArgs = new ArrayList<>();
  protected final List<HttpResponseAssertion> assertions = new ArrayList<>();
  protected RequestBody requestBody = null;
  protected Serializer serializer = null;
  protected Boolean retryOverride = null;
  protected String nameOverride = null;
  protected Supplier<String> bearerToken = null;

  protected AbstractRestStateBuilderIdentity(TransitionBuilder tb) {
    this.transitionBuilder = tb;
    this.identityKey = this;
    tb.addFutureStep(this::buildState, this.identityKey);
  }

  protected AbstractRestStateBuilderIdentity(TransitionBuilder tb, Object key, AbstractRestStateBuilderIdentity<?> copyFrom) {
    this.transitionBuilder = tb;
    this.identityKey = key;
    this.clientProducer = copyFrom.clientProducer;
    this.baseUrlProducer = copyFrom.baseUrlProducer;
    this.verb = copyFrom.verb;
    this.pathProducer = copyFrom.pathProducer;
    this.queryArgs.addAll(copyFrom.queryArgs);
    this.assertions.addAll(copyFrom.assertions);
    this.requestBody = copyFrom.requestBody;
    this.serializer = copyFrom.serializer;
    this.retryOverride = copyFrom.retryOverride;
    this.nameOverride = copyFrom.nameOverride;
    this.bearerToken = copyFrom.bearerToken;
    tb.addFutureStep(this::buildState, key);
  }

  protected abstract T self();

  protected abstract RestHttpState buildState();

  protected String resolveName() {
    if (nameOverride != null) return nameOverride;
    String url;
    try { url = buildUrlProducer().get().toString(); } catch (Exception e) { url = "promised url"; }
    return "REST %s request to %s".formatted(verb, url);
  }

  protected boolean resolveRetry() {
    if (retryOverride != null) return retryOverride;
    return switch (verb) {
      case GET, PUT, DELETE -> true;
      case POST, PATCH -> false;
    };
  }

  protected Supplier<URI> buildUrlProducer() {
    return () -> {
      URI base = baseUrlProducer.get();
      String path = pathProducer.get();
      if (queryArgs.isEmpty()) return base.resolve(path);
      String query = queryArgs.stream()
          .map(Supplier::get)
          .map(kv -> encode(kv[0]) + "=" + encode(kv[1]))
          .reduce((a, b) -> a + "&" + b)
          .orElse("");
      return base.resolve(path + "?" + query);
    };
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public T withCustomClient(HttpClient client) {
    this.clientProducer = () -> client;
    return self();
  }

  public T withCustomClient(Supplier<HttpClient> clientProducer) {
    this.clientProducer = clientProducer;
    return self();
  }

  public T withCustomBaseUrl(URI uri) {
    this.baseUrlProducer = () -> uri;
    return self();
  }

  public T withCustomBaseUrl(Supplier<URI> uriProducer) {
    this.baseUrlProducer = uriProducer;
    return self();
  }

  public T withHttpVerb(HttpMethodVerb verb) {
    this.verb = verb;
    return self();
  }

  public T withPathString(String path) {
    this.pathProducer = () -> path;
    return self();
  }

  public T withPathString(Supplier<String> pathProducer) {
    this.pathProducer = pathProducer;
    return self();
  }

  public T addQueryArgument(String key, String value) {
    this.queryArgs.add(() -> new String[]{key, value});
    return self();
  }

  public T addQueryArgument(Supplier<String[]> argument) {
    this.queryArgs.add(argument);
    return self();
  }

  public T withSerializer(Serializer serializer) {
    this.serializer = serializer;
    return self();
  }

  public T withCustomName(String name) {
    this.nameOverride = name;
    return self();
  }

  public T withRetry(boolean retry) {
    this.retryOverride = retry;
    return self();
  }

  public <TRequest> T withRequestBody(Supplier<TRequest> bodySupplier) {
    Serializer s = this.serializer;
    this.requestBody = new RequestBody(
        () -> bodySupplier.get(),
        obj -> {
          if (s == null)
            throw new IllegalStateException("No serializer defined; call withSerializer() or use withRequestBody(supplier, serializationFn)");
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

  public T assertThatResponse(HttpResponseAssertion assertion) {
    this.assertions.add(assertion);
    return self();
  }

  public T assertSuccess() {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code < 200 || code >= 300)
        throw new XceptoTestFailedException("Expected success (2xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertClientFailure() {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code < 400 || code >= 500)
        throw new XceptoTestFailedException("Expected client failure (4xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertServerFailure() {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code < 500 || code >= 600)
        throw new XceptoTestFailedException("Expected server failure (5xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertThatResponseStatus(int expectedStatus) {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code != expectedStatus)
        throw new XceptoTestFailedException("Expected status %d but got %d for %s".formatted(expectedStatus, code, response.uri()));
    });
  }

  public T assertThatResponseContentString(java.util.function.Predicate<String> predicate) {
    return assertThatResponse(response -> {
      String body = response.body();
      if (!predicate.test(body))
        throw new XceptoTestFailedException("Response body did not match predicate. Body was: %s".formatted(body));
    });
  }

  public <TResponse> DeserializedResponseRestStateBuilderIdentity<TResponse> withResponseType(Class<TResponse> type) {
    return new DeserializedResponseRestStateBuilderIdentity<>(transitionBuilder, identityKey, this, type);
  }
}
