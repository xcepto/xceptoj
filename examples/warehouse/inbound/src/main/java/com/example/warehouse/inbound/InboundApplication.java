package com.example.warehouse.inbound;

import com.example.warehouse.common.RabbitMqRetryConfig;
import com.example.warehouse.events.ShipmentArrivedEvent;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class InboundApplication {


  public static final String shipmentArrivedQueue = ShipmentArrivedEvent.class.getName();
  @Value("${spring.rabbitmq.host}")
  private String host;
  @Value("${spring.rabbitmq.port}")
  private int port;
  @Value("${spring.rabbitmq.username}")
  private String username;
  @Value("${spring.rabbitmq.password}")
  private String password;

  public static void main(String[] args) {
    SpringApplication.run(InboundApplication.class, args);
  }

  @Bean
  Queue shipmentArrivedQueue() {
    return new Queue(shipmentArrivedQueue, false);
  }

  @Bean
  public DirectExchange warehouseExchange() {
    return new DirectExchange("warehouse", true, false);
  }

  @Bean
  public Binding binding(@Qualifier("shipmentArrivedQueue") Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(queue.getName());
  }

  @Bean
  Queue queue() {
    return new Queue(shipmentArrivedQueue, false);
  }

  @Bean
  public ConnectionFactory rabbitConnectionFactory() throws Exception {

    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setPort(port);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);

    RetryConfig retryConfig = RabbitMqRetryConfig.getRetryConfig();
    Retry retry = Retry.of("retry", retryConfig);
    AtomicInteger retries = new AtomicInteger();
    Callable<Void> callable = () -> {
      System.out.println("Retry count: " + retries.get());
      retries.addAndGet(1);
      try (var connection = connectionFactory.createConnection()) {
        if (!connection.isOpen())
          throw new Exception();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);
    retriedStart.call();

    return connectionFactory;
  }

}
