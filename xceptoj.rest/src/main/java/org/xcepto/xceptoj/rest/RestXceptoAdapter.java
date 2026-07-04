package org.xcepto.xceptoj.rest;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.rest.builders.RestStateBuilderIdentity;
import org.xcepto.xceptoj.rest.data.HttpMethodVerb;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.function.Supplier;

public class RestXceptoAdapter extends XceptoAdapter {
  private final HttpClient httpClient;
  private final URI baseUrl;
  private final Serializer serializer;
  private TransitionBuilder transitionBuilder;

  public RestXceptoAdapter(HttpClient httpClient, URI baseUrl, Serializer serializer) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.serializer = serializer;
  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }

  private RestStateBuilderIdentity inject(RestStateBuilderIdentity identity, HttpMethodVerb verb, Supplier<String> pathProducer) {
    if (baseUrl != null) identity.withCustomBaseUrl(baseUrl);
    if (serializer != null) identity.withSerializer(serializer);
    identity.withCustomClient(httpClient);
    identity.withHttpVerb(verb);
    identity.withPathString(pathProducer);
    return identity;
  }

  public RestStateBuilderIdentity get(String path) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.GET, () -> path);
  }

  public RestStateBuilderIdentity get(Supplier<String> pathProducer) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.GET, pathProducer);
  }

  public RestStateBuilderIdentity post(String path) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.POST, () -> path);
  }

  public RestStateBuilderIdentity post(Supplier<String> pathProducer) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.POST, pathProducer);
  }

  public RestStateBuilderIdentity patch(String path) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.PATCH, () -> path);
  }

  public RestStateBuilderIdentity patch(Supplier<String> pathProducer) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.PATCH, pathProducer);
  }

  public RestStateBuilderIdentity put(String path) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.PUT, () -> path);
  }

  public RestStateBuilderIdentity put(Supplier<String> pathProducer) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.PUT, pathProducer);
  }

  public RestStateBuilderIdentity delete(String path) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.DELETE, () -> path);
  }

  public RestStateBuilderIdentity delete(Supplier<String> pathProducer) {
    return inject(new RestStateBuilderIdentity(transitionBuilder), HttpMethodVerb.DELETE, pathProducer);
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {}

  @Override
  protected void addServices(ServiceCollection serviceCollection) {}

  @Override
  protected void terminate() throws XceptoAdapterTerminationException {}
}
