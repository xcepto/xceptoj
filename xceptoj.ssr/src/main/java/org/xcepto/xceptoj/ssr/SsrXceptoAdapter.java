package org.xcepto.xceptoj.ssr;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.ssr.builders.SsrStateBuilderIdentity;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.function.Supplier;

public class SsrXceptoAdapter extends XceptoAdapter {

  private final HttpClient httpClient;
  private final URI baseUrl;
  private TransitionBuilder transitionBuilder;

  public SsrXceptoAdapter(HttpClient httpClient, URI baseUrl) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }

  private SsrStateBuilderIdentity inject(SsrStateBuilderIdentity identity, HttpMethodVerb verb,
      Supplier<String> pathProducer) {
    if (baseUrl != null) identity.withCustomBaseUrl(baseUrl);
    identity.withCustomClient(httpClient);
    identity.withHttpVerb(verb);
    identity.withPathString(pathProducer);
    return identity;
  }

  public SsrStateBuilderIdentity get(String path) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), HttpMethodVerb.GET, () -> path);
  }

  public SsrStateBuilderIdentity get(Supplier<String> pathProducer) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), HttpMethodVerb.GET, pathProducer);
  }

  public SsrStateBuilderIdentity post(String path) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), HttpMethodVerb.POST, () -> path);
  }

  public SsrStateBuilderIdentity post(Supplier<String> pathProducer) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), HttpMethodVerb.POST, pathProducer);
  }

  public SsrStateBuilderIdentity request(String path, HttpMethodVerb verb) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), verb, () -> path);
  }

  public SsrStateBuilderIdentity request(Supplier<String> pathProducer, HttpMethodVerb verb) {
    return inject(new SsrStateBuilderIdentity(transitionBuilder), verb, pathProducer);
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {}

  @Override
  protected void addServices(ServiceCollection serviceCollection) {}

  @Override
  protected void terminate() throws XceptoAdapterTerminationException {}
}
