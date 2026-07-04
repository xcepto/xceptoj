package org.xcepto.xceptoj.http.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.http.data.HttpResponseAssertion;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractHttpStateBuilderIdentity<T extends AbstractHttpStateBuilderIdentity<T>> {

  protected final TransitionBuilder transitionBuilder;
  protected final Object identityKey;

  protected Supplier<HttpClient> clientProducer = HttpClient::newHttpClient;
  protected Supplier<URI> baseUrlProducer = () -> URI.create("http://localhost:8080");
  protected HttpMethodVerb verb = HttpMethodVerb.GET;
  protected Supplier<String> pathProducer = () -> "/";
  protected final List<Supplier<String[]>> queryArgs = new ArrayList<>();
  protected final List<HttpResponseAssertion> assertions = new ArrayList<>();
  protected Boolean retryOverride = null;
  protected String nameOverride = null;

  protected AbstractHttpStateBuilderIdentity(TransitionBuilder tb) {
    this.transitionBuilder = tb;
    this.identityKey = this;
    tb.addFutureStep(this::buildState, this.identityKey);
  }

  protected AbstractHttpStateBuilderIdentity(TransitionBuilder tb, Object key,
      AbstractHttpStateBuilderIdentity<?> copyFrom) {
    this.transitionBuilder = tb;
    this.identityKey = key;
    this.clientProducer = copyFrom.clientProducer;
    this.baseUrlProducer = copyFrom.baseUrlProducer;
    this.verb = copyFrom.verb;
    this.pathProducer = copyFrom.pathProducer;
    this.queryArgs.addAll(copyFrom.queryArgs);
    this.assertions.addAll(copyFrom.assertions);
    this.retryOverride = copyFrom.retryOverride;
    this.nameOverride = copyFrom.nameOverride;
    tb.addFutureStep(this::buildState, key);
  }

  protected abstract T self();

  protected abstract XceptoState buildState();

  protected String adapterPrefix() { return "HTTP"; }

  protected String resolveName() {
    if (nameOverride != null) return nameOverride;
    String url;
    try { url = buildUrlProducer().get().toString(); } catch (Exception e) { url = "promised url"; }
    return "%s %s request to %s".formatted(adapterPrefix(), verb, url);
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

  public T withCustomName(String name) {
    this.nameOverride = name;
    return self();
  }

  public T withRetry(boolean retry) {
    this.retryOverride = retry;
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
        throw new XceptoTestFailedException(
            "Expected success (2xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertClientFailure() {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code < 400 || code >= 500)
        throw new XceptoTestFailedException(
            "Expected client failure (4xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertServerFailure() {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code < 500 || code >= 600)
        throw new XceptoTestFailedException(
            "Expected server failure (5xx) but got %d for %s".formatted(code, response.uri()));
    });
  }

  public T assertThatResponseStatus(int expectedStatus) {
    return assertThatResponse(response -> {
      int code = response.statusCode();
      if (code != expectedStatus)
        throw new XceptoTestFailedException(
            "Expected status %d but got %d for %s".formatted(expectedStatus, code, response.uri()));
    });
  }

  public T assertThatResponseContentString(Predicate<String> predicate) {
    return assertThatResponse(response -> {
      String body = response.body();
      if (!predicate.test(body))
        throw new XceptoTestFailedException(
            "Response body did not match predicate. Body was: %s".formatted(body));
    });
  }
}
