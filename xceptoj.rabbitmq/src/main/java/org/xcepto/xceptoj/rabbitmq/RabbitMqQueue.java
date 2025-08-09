package org.xcepto.xceptoj.rabbitmq;

import java.lang.reflect.Type;
import java.util.function.Function;

public class RabbitMqQueue {
  Function<String, Object> parser;
  boolean durable;
  Type schemaType;
  String name;
  String routingKey;

  RabbitMqQueue(String name, String routingKey, Type schemaType, boolean durable, Function<String, Object> parser) {
    this.parser = parser;
    this.durable = durable;
    this.schemaType = schemaType;
    this.routingKey = routingKey;
    this.name = name;
  }
}