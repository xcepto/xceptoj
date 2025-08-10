package org.xcepto.xceptoj.rabbitmq;

import org.xcepto.xceptoj.TransitionBuilder;
import org.xcepto.xceptoj.XceptoAdapter;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.interfaces.LoggingProvider;
import org.xcepto.xceptoj.interfaces.ServiceCollection;
import org.xcepto.xceptoj.interfaces.ServiceProvider;
import com.rabbitmq.client.*;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RabbitMqXceptoAdapter extends XceptoAdapter {

  private final RabbitMqConfig config;
  private final RetryConfig retryConfig;
  private final RabbitMqRepository repository = new RabbitMqRepository();

  private Connection connection;
  private TransitionBuilder transitionBuilder;


  public RabbitMqXceptoAdapter(RabbitMqConfig config, RetryConfig retryConfig) {
    this.config = config;
    this.retryConfig = retryConfig;
  }

  public RabbitMqXceptoAdapter(RabbitMqConfig config) {
    this.config = config;
    this.retryConfig = getDefaultRetryConfig();
  }

  private RetryConfig getDefaultRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(6)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2.0))
        .build();
  }

  public <TEvent> void eventCondition(Class<TEvent> schema, Predicate<TEvent> predicate) {
    Predicate<ServiceProvider> validation = serviceProvider -> {
      var repository = serviceProvider.get(RabbitMqRepository.class);
      var loggingProvider = serviceProvider.get(LoggingProvider.class);

      Optional<Object> optionalMessage = repository.dequeueMessage(schema);
      if (optionalMessage.isEmpty())
        return false;
      TEvent event;
      try {
        event = schema.cast(optionalMessage.get());
        boolean test = predicate.test(event);
        if (!test) {
          loggingProvider.logDebug("%s message did not meet predicate requirements".formatted(schema.getName()));
          return false;
        }
        return true;
      } catch (ClassCastException e) {
        throw new RuntimeException("Message from rabbitmq was not castable to %s".formatted(schema.getName()));
      }
    };

    String stateName = "EventConditionStepFor%s".formatted(schema.getSimpleName());
    transitionBuilder.addStep(new RabbitMqXceptoState(stateName, validation));
  }

  @Override
  protected void initialize(ServiceProvider serviceProvider) throws XceptoAdapterInitializationException {
    Retry retry = Retry.of("retry", retryConfig);
    AtomicInteger retries = new AtomicInteger();
    LoggingProvider loggingProvider = serviceProvider.get(LoggingProvider.class);
    Callable<Void> callable = () -> {
      loggingProvider.logDebug("RabbitMq initialization retry count: " + retries.get());
      retries.addAndGet(1);
      rabbitMqInitialization(serviceProvider);
      return null;
    };
    Callable<Void> retriedStart = retry.decorateCallable(callable);
    try {
      retriedStart.call();
    } catch (Exception e) {
      throw new XceptoAdapterInitializationException("Rabbitmq adapter failed Initialization after %d retries!".formatted(retries.get()));
    }
  }

  private void rabbitMqInitialization(ServiceProvider serviceProvider) throws IOException, TimeoutException {
    RabbitMqRepository repository = serviceProvider.get(RabbitMqRepository.class);

    var factory = new ConnectionFactory();
    factory.setHost(config.hostname);
    factory.setPort(config.port);
    factory.setUsername(config.username);
    factory.setPassword(config.password);
    LoggingProvider loggingProvider = serviceProvider.get(LoggingProvider.class);
    loggingProvider.logDebug("RabbitMq adapter connecting to %s:%d".formatted(config.hostname, config.port));
    try {
      connection = factory.newConnection();
      Channel channel = connection.createChannel();

      for (RabbitMqExchange exchange : config.getExchanges()) {

        // cannot declare default exchange
        if (!Objects.equals(exchange.name, ""))
          channel.exchangeDeclare(exchange.name, exchange.type, exchange.durable);

        for (RabbitMqQueue queue : exchange.queues.values()) {
          channel.queueDeclare(queue.name, queue.durable, false, false, Map.of());
          channel.queueBind(queue.name, exchange.name, queue.routingKey);

          DeliverCallback deliverCallback = (tag, message) -> {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            loggingProvider.logDebug("Event arrived on queue %s with body %s".formatted(queue.name, body));
            Object event = queue.parser.apply(body);
            repository.enqueueMessage(queue.schemaType, event);
          };

          CancelCallback cancelCallback = tag -> {

          };

          channel.basicConsume(queue.name, true, deliverCallback, cancelCallback);
        }
      }
    } catch (IOException e) {
      loggingProvider.logDebug("Failed to initialize because: %s".formatted(e.getCause()));
      throw e;
    }

  }

  @Override
  protected void addServices(ServiceCollection serviceCollection) {
    serviceCollection.register(RabbitMqRepository.class, () -> repository);
  }

  @Override
  protected void terminate() throws XceptoAdapterTerminationException {
    try {
      connection.close();
    } catch (IOException e) {
      throw new XceptoAdapterTerminationException(e.getCause().toString());
    }
  }

  @Override
  protected void injectBuilder(TransitionBuilder builder) {
    this.transitionBuilder = builder;
  }
}
