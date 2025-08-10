package com.example.warehouse.inbound;

import com.example.warehouse.events.StockReplenishedEvent;
import com.example.warehouse.inbound.requests.AcceptShipmentRequest;
import com.example.warehouse.inbound.requests.AcceptShipmentResponse;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class JunitTests {

  @Test
  public void simpleSequentialTest() throws Exception {

    // Given
    InboundFlowScenario scenario = new InboundFlowScenario();
    scenario.startEnvironment();

    // Arrange
    Gson gson = new Gson();
    AcceptShipmentRequest request = new AcceptShipmentRequest();
    request.amount = 50;
    URI url = URI.create("http://localhost:%d/shipment/accept"
        .formatted(scenario.getPort("examples.warehouse.inbound", 8081)));
    RetryConfig retryConfig = RetryConfig.custom()
        .maxAttempts(6)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2.0))
        .build();
    String eventName = StockReplenishedEvent.class.getName();
    String queueName = "junit-" + StockReplenishedEvent.class.getName();
    var factory = new ConnectionFactory();
    factory.setHost("localhost");
    factory.setPort(scenario.getPort("rabbitmq", 5672));
    factory.setUsername("guest");
    factory.setPassword("guest");
    AtomicReference<StockReplenishedEvent> stockReplenishedEvent = new AtomicReference<>();
    Retry retry = Retry.of("retry", retryConfig);
    AtomicInteger retries = new AtomicInteger();
    AtomicReference<Connection> connection = new AtomicReference<>();
    Callable<Void> callable = () -> {
      System.out.println("RabbitMq initialization retry count: " + retries.get());
      retries.addAndGet(1);
      connection.set(factory.newConnection());
      Channel channel = connection.get().createChannel();
      channel.exchangeDeclare("warehouse", BuiltinExchangeType.DIRECT, true);
      channel.queueDeclare(queueName, true, false, false, Map.of());
      channel.queueBind(queueName, "warehouse", eventName);
      DeliverCallback deliverCallback = (tag, message) -> {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        System.out.printf("Event arrived on queue %s with body %s%n", queueName, body);
        stockReplenishedEvent.set(gson.fromJson(body, StockReplenishedEvent.class));
      };
      CancelCallback cancelCallback = tag -> {
      };
      channel.basicConsume(queueName, true, deliverCallback, cancelCallback);
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);
    retriedStart.call();


    // When(AcceptShipment)
    HttpRequest postRequest = HttpRequest.newBuilder()
        .uri(url)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
        .build();
    AcceptShipmentResponse response;
    try (HttpClient httpClient = HttpClient.newHttpClient()) {

      Retry restRetry = Retry.of("retry", retryConfig);
      AtomicInteger restRetries = new AtomicInteger();
      Supplier<HttpResponse<String>> httpResponseSupplier = () -> {
        System.out.println("Rest request retry count: " + restRetries.get());
        retries.addAndGet(1);
        try {
          return httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      };
      Supplier<HttpResponse<String>> retriedSupplier = restRetry.decorateSupplier(httpResponseSupplier);

      HttpResponse<String> httpResponse = retriedSupplier.get();
      String responseJson = new String(httpResponse.body().getBytes(), StandardCharsets.UTF_8);
      response = gson.fromJson(responseJson, AcceptShipmentResponse.class);
    }

    Duration timeout = Duration.ofSeconds(30);
    Thread.sleep(timeout);

    connection.get().close();
    scenario.stopEnvironment();

    assert response.amount == request.amount;

    // Then(StockReplenished)
    assert stockReplenishedEvent.get().total == request.amount;
  }
}
