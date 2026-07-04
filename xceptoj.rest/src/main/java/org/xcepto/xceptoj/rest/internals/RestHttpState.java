package org.xcepto.xceptoj.rest.internals;

import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.data.HttpMethodVerb;
import org.xcepto.xceptoj.http.data.HttpResponseAssertion;
import org.xcepto.xceptoj.http.internals.AbstractHttpState;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import org.xcepto.xceptoj.rest.data.RequestBody;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

public class RestHttpState extends AbstractHttpState {

  private final Supplier<URI> urlProducer;
  private final Supplier<HttpClient> clientProducer;
  private final Supplier<String> bearerToken;
  private final HttpMethodVerb verb;
  private final RequestBody requestBody;

  public RestHttpState(String name,
                       Supplier<URI> urlProducer,
                       Supplier<HttpClient> clientProducer,
                       Supplier<String> bearerToken,
                       HttpMethodVerb verb,
                       boolean retry,
                       RequestBody requestBody,
                       List<HttpResponseAssertion> assertions,
                       ResponseAction responseAction) {
    super(name, assertions, retry, responseAction);
    this.urlProducer = urlProducer;
    this.clientProducer = clientProducer;
    this.bearerToken = bearerToken;
    this.verb = verb;
    this.requestBody = requestBody;
  }

  @Override
  protected HttpResponse<String> executeRequest(ServiceProvider sp) throws XceptoTestFailedException {
    URI url = urlProducer.get();
    LoggingProvider logger = sp.get(LoggingProvider.class);
    logger.logDebug("Send %s REST request to %s".formatted(verb, url));

    String bodyStr = requestBody != null ? requestBody.serialize() : "";
    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyStr, StandardCharsets.UTF_8);

    HttpRequest.Builder req = HttpRequest.newBuilder()
        .uri(url)
        .header("Content-Type", "application/json");

    if (bearerToken != null) {
      req.header("Authorization", "Bearer " + bearerToken.get());
    }

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
      throw new XceptoTestFailedException("HTTP %s request to %s failed: %s".formatted(verb, url, e.getMessage()), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new XceptoTestFailedException("HTTP %s request to %s was interrupted".formatted(verb, url), e);
    }
  }
}
