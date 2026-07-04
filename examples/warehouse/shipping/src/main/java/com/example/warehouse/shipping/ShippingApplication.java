package com.example.warehouse.shipping;

import com.example.warehouse.common.RabbitMqRetryConfig;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ShippingApplication {

  public static final String EXCHANGE = "warehouse";

  public static void main(String[] args) throws Exception {
    String host = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
    int port = Integer.parseInt(System.getenv().getOrDefault("RABBITMQ_PORT", "5672"));
    String username = System.getenv().getOrDefault("RABBITMQ_USERNAME", "guest");
    String password = System.getenv().getOrDefault("RABBITMQ_PASSWORD", "guest");

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(port);
    factory.setUsername(username);
    factory.setPassword(password);


    RetryConfig config = RabbitMqRetryConfig.getRetryConfig();
    Retry retry = Retry.of("retry", config);
    AtomicInteger retries = new AtomicInteger();
    Callable<Void> callable = () -> {
      System.out.println("Retry count: " + retries.get());
      retries.addAndGet(1);
      start(factory);
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);

    retriedStart.call();
  }

  static void start(ConnectionFactory factory) throws IOException, TimeoutException, InterruptedException {
    try (Connection connection = factory.newConnection()) {
      System.out.println("started up");

      Channel setupChannel = connection.createChannel();
      setupChannel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true);

      new Thread(() -> {
        try {
          StockReservedConsumer consumer = new StockReservedConsumer(connection);
          consumer.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
      Thread.currentThread().join();
    }
  }
}
