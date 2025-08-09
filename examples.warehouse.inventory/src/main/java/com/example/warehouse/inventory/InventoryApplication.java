package com.example.warehouse.inventory;

import com.example.warehouse.common.RabbitMqRetryConfig;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryApplication {

  public static String Exchange = "warehouse";

  public static void main(String[] args) throws Exception {

    Map<String, String> environment = System.getenv();
    String rabbitMqHost = environment.getOrDefault("RABBITMQ_HOST", "localhost");
    int rabbitMqPort = Integer.parseInt(environment.getOrDefault("RABBITMQ_PORT", "5672"));
    String rabbitMqUsername = environment.getOrDefault("RABBITMQ_USERNAME", "guest");
    String rabbitMqPassword = environment.getOrDefault("RABBITMQ_PASSWORD", "guest");

    String postgresUrl = environment.getOrDefault("POSTGRES_URL", "jdbc:postgresql://localhost:5432/warehouse");
    String postgresUsername = environment.getOrDefault("POSTGRES_USERNAME", "postgres");
    String postgresPassword = environment.getOrDefault("POSTGRES_PASSWORD", "postgres");

    PostgresConnectionFactory postgresConnectionFactory = new PostgresConnectionFactory();
    postgresConnectionFactory.setUrl(postgresUrl);
    postgresConnectionFactory.setUsername(postgresUsername);
    postgresConnectionFactory.setPassword(postgresPassword);

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMqHost);
    factory.setPort(rabbitMqPort);
    factory.setUsername(rabbitMqUsername);
    factory.setPassword(rabbitMqPassword);


    RetryConfig config = RabbitMqRetryConfig.getRetryConfig();
    Retry retry = Retry.of("retry", config);
    AtomicInteger retries = new AtomicInteger();
    Callable<Void> callable = () -> {
      System.out.println("Retry count: " + retries.get());
      retries.addAndGet(1);
      start(factory, postgresConnectionFactory);
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);

    retriedStart.call();
  }

  static void start(ConnectionFactory factory, PostgresConnectionFactory postgresConnectionFactory) throws IOException, TimeoutException, InterruptedException, SQLException {

    try(java.sql.Connection setupConnection = postgresConnectionFactory.Build()){
      Statement statement = setupConnection.createStatement();
      String table = """
          CREATE TABLE IF NOT EXISTS inventory (
              id SERIAL PRIMARY KEY,
              change INT NOT NULL,
              created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
          );""";
      statement.execute(table);
    } catch (Exception e) {
      System.out.println("Failed to initialize postgres: "+ e.getCause() + ", message: "+ e.getMessage());
      throw e;
    }

    try (Connection connection = factory.newConnection()) {
      System.out.println("started up");

      Channel setupChannel = connection.createChannel();
      setupChannel.exchangeDeclare("warehouse", BuiltinExchangeType.DIRECT, true);

      OrderPlacedConsumer orderPlacedConsumer = new OrderPlacedConsumer(connection, postgresConnectionFactory);
      orderPlacedConsumer.start();

      ShipmentArrivedConsumer shipmentArrivedConsumer = new ShipmentArrivedConsumer(connection, postgresConnectionFactory);
      shipmentArrivedConsumer.start();

      Thread.currentThread().join();
    } catch (Exception e) {
      System.out.println("Failed to initialize rabbitmq: "+ e.getCause());
      throw e;
    }
  }
}
