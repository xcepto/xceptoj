package org.xcepto.xceptoj.rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RabbitMqExchange {
  private final Gson gson = new Gson();
  String name;
  BuiltinExchangeType type;
  boolean durable;
  Map<String, RabbitMqQueue> queues = new HashMap<>();

  public RabbitMqExchange(String name, BuiltinExchangeType type, boolean durable) {
    this.durable = durable;
    this.name = name;
    this.type = type;
  }

  public <TSchema> void bindEvent(Class<TSchema> schema, String routingKey, boolean durable, Function<String, TSchema> parser) {
    if (parser == null)
      parser = str -> (TSchema) gson.fromJson(str, schema);
    var typeName = schema.getName();
    Function<String, TSchema> finalParser = parser;
    String queueName = "xcepto-%s".formatted(typeName);
    RabbitMqQueue rabbitMqQueue = new RabbitMqQueue(queueName, routingKey, schema, durable, finalParser::apply);
    queues.put(typeName, rabbitMqQueue);
  }
}