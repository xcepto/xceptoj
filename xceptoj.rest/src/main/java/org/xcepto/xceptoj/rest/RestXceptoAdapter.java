package org.xcepto.xceptoj.rest;

import com.google.gson.Gson;
import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;

import java.net.URI;
import java.util.function.Predicate;

public class RestXceptoAdapter extends XceptoAdapter {

  private final RetryConfig retryConfig;
  private final Gson gson = new Gson();

  private TransitionBuilder transitionBuilder;

  public RestXceptoAdapter(RetryConfig retryConfig) {
    this.retryConfig = retryConfig;
  }

  public RestXceptoAdapter() {
    this.retryConfig = getDefaultRetryConfig();
  }

  private RetryConfig getDefaultRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(6)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2.0))
        .build();
  }

  public <TRequest, TResponse> void postRequest(URI url, TRequest request, Class<TResponse> responseType, Predicate<TResponse> responsePredicate) {
    RestResponseValidator validator = response -> {
      try {
        TResponse castedResponse = responseType.cast(response);
        if (responsePredicate.test(castedResponse))
          return true;
        String stringifiedResponse = gson.toJson(castedResponse);
        throw new XceptoTestFailedException("Response validation of failed, with response: %s".formatted(stringifiedResponse));
      } catch (ClassCastException classCastException) {
        throw new XceptoTestFailedException("Response from rest endpoint was not castable to %s".formatted(responseType.getName()));
      }
    };

    XceptoState postRequestState = new PostRequestXceptoState(
        "%sPostRequestState".formatted(responseType.getName()),
        url,
        gson.toJson(request),
        validator,
        responseType,
        retryConfig
    );
    transitionBuilder.addStep(postRequestState);
  }

  public void getHtmlCondition(URI url, Predicate<String> responsePredicate) {
    RestResponseValidator validator = response -> {
      String responseHtml = response.toString();
      if (responsePredicate.test(responseHtml))
        return true;
      throw new XceptoTestFailedException("Response validation of failed, with response: %s".formatted(responseHtml));
    };

    XceptoState getHtmlCondition = new GetHtmlConditionXceptoState(
        "GetHtmlConditionState",
        url,
        validator,
        retryConfig
    );
    transitionBuilder.addStep(getHtmlCondition);
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) {

  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {

  }

  @Override
  protected void terminate() {

  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }
}
