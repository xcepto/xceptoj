package org.xcepto.xceptoj.rest;

import com.google.gson.JsonSyntaxException;
import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GetHtmlConditionXceptoState extends XceptoState {
  public GetHtmlConditionXceptoState(String name, URI url, RestResponseValidator restResponseValidator, RetryConfig retryConfig) {
    super(name);
    this.url = url;
    this.restResponseValidator = restResponseValidator;
    this.retryConfig = retryConfig;
  }

  private final URI url;
  private final RestResponseValidator restResponseValidator;
  private final RetryConfig retryConfig;
  private Object response;


  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) throws XceptoTestFailedException {
    HttpRequest postRequest = HttpRequest.newBuilder()
        .uri(url)
        .header("Content-Type", "application/json")
        .GET()
        .build();
    String responseText = null;
    try (HttpClient httpClient = HttpClient.newHttpClient()) {

      Retry retry = Retry.of("retry", retryConfig);
      AtomicInteger retries = new AtomicInteger();
      Supplier<HttpResponse<String>> httpResponseSupplier = () -> {
        System.out.println("Rest request retry count: " + retries.get());
        retries.addAndGet(1);
        try {
          return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      };
      Supplier<HttpResponse<String>> retriedSupplier = retry.decorateSupplier(httpResponseSupplier);

      HttpResponse<String> httpResponse = retriedSupplier.get();
      responseText = new String(httpResponse.body().getBytes(), StandardCharsets.UTF_8);
      response = responseText;
    } catch (JsonSyntaxException e) {
      throw new XceptoTestFailedException("Failed to deserialize Post Response. Response content was: %s".formatted(responseText));
    } catch (RuntimeException e) {
      throw new XceptoTestFailedException("Failed to execute Post Request: %s".formatted(e.getCause()));
    }
    return restResponseValidator.test(response);
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) throws XceptoTestFailedException {

  }
}
