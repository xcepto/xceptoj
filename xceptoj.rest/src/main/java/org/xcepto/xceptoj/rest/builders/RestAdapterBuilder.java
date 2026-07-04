package org.xcepto.xceptoj.rest.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.rest.RestXceptoAdapter;
import org.xcepto.xceptoj.rest.Serializer;

import java.net.URI;
import java.net.http.HttpClient;

public class RestAdapterBuilder {
  private final TransitionBuilder transitionBuilder;
  private HttpClient httpClient = HttpClient.newHttpClient();
  private URI baseUrl = null;
  private Serializer serializer = null;

  public RestAdapterBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }

  public RestAdapterBuilder withHttpClient(HttpClient client) {
    this.httpClient = client;
    return this;
  }

  public RestAdapterBuilder withBaseUrl(URI url) {
    this.baseUrl = url;
    return this;
  }

  public RestAdapterBuilder withSerializer(Serializer serializer) {
    this.serializer = serializer;
    return this;
  }

  public RestXceptoAdapter build() {
    var adapter = new RestXceptoAdapter(httpClient, baseUrl, serializer);
    transitionBuilder.registerAdapter(adapter);
    return adapter;
  }
}
