package com.example.warehouse.inbound.config;

import com.example.warehouse.events.StockReplenishedEvent;
import org.xcepto.xceptoj.rabbitmq.RabbitMqConfig;
import org.xcepto.xceptoj.rabbitmq.RabbitMqExchange;
import com.rabbitmq.client.BuiltinExchangeType;

public class WarehouseRabbitMqConfig {

  public static RabbitMqConfig getConfig(int mappedPort) {
    RabbitMqConfig config = new RabbitMqConfig();
    config.setHostName("localhost");
    config.setPort(mappedPort);
    config.setUsername("guest");
    config.setPassword("guest");
    RabbitMqExchange exchange = new RabbitMqExchange("warehouse", BuiltinExchangeType.DIRECT, true);
    config.addExchange(exchange);
    exchange.bindEvent(StockReplenishedEvent.class, StockReplenishedEvent.class.getName(), true, null);

    return config;
  }
}
