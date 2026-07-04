package org.xcepto.xceptoj.ssr.internals;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.http.data.HttpResponseAssertion;
import org.xcepto.xceptoj.http.internals.AbstractHttpState;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SsrHttpState extends AbstractHttpState {

  private final Supplier<URI> urlProducer;
  private final Supplier<HttpClient> clientProducer;
  private final HttpMethodVerb verb;
  private final Supplier<Map<String, String>> formContent;

  public SsrHttpState(String name,
                      Supplier<URI> urlProducer,
                      Supplier<HttpClient> clientProducer,
                      HttpMethodVerb verb,
                      boolean retry,
                      Supplier<Map<String, String>> formContent,
                      List<HttpResponseAssertion> assertions,
                      ResponseAction responseAction) {
    super(name, assertions, retry, responseAction);
    this.urlProducer = urlProducer;
    this.clientProducer = clientProducer;
    this.verb = verb;
    this.formContent = formContent;
  }

  @Override
  protected HttpResponse<String> executeRequest(ServiceProvider sp) throws XceptoTestFailedException {
    URI url = urlProducer.get();
    LoggingProvider logger = sp.get(LoggingProvider.class);
    logger.logDebug("Send %s SSR request to %s".formatted(verb, url));

    String bodyStr = "";
    if (formContent != null) {
      bodyStr = formContent.get().entrySet().stream()
          .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
          .collect(Collectors.joining("&"));
    }

    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8);
    HttpRequest.Builder req = HttpRequest.newBuilder()
        .uri(url)
        .header("Content-Type", "application/x-www-form-urlencoded");

    HttpRequest request = switch (verb) {
      case GET -> req.GET().build();
      case POST -> req.POST(bodyPublisher).build();
      case PATCH -> req.method("PATCH", bodyPublisher).build();
      case PUT -> req.PUT(bodyPublisher).build();
      case DELETE -> req.DELETE().build();
    };

    try {
      return clientProducer.get().send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new XceptoTestFailedException("HTTP %s SSR request to %s failed: %s".formatted(verb, url, e.getMessage()), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new XceptoTestFailedException("HTTP %s SSR request to %s was interrupted".formatted(verb, url), e);
    }
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
