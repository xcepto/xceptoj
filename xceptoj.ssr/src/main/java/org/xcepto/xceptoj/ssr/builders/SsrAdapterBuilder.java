package org.xcepto.xceptoj.ssr.builders;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.ssr.SsrXceptoAdapter;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class SsrAdapterBuilder {

  private final TransitionBuilder transitionBuilder;
  private HttpClient httpClient = null;
  private URI baseUrl = null;

  public SsrAdapterBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }

  public SsrAdapterBuilder withHttpClient(HttpClient client) {
    this.httpClient = client;
    return this;
  }

  public SsrAdapterBuilder withBaseUrl(URI url) {
    this.baseUrl = url;
    return this;
  }

  public SsrXceptoAdapter build() {
    if (httpClient == null) {
      httpClient = HttpClient.newBuilder()
          .cookieHandler(new CookieManager())
          .connectTimeout(Duration.ofSeconds(10))
          .build();
    }
    var adapter = new SsrXceptoAdapter(httpClient, baseUrl);
    transitionBuilder.registerAdapter(adapter);
    return adapter;
  }
}
