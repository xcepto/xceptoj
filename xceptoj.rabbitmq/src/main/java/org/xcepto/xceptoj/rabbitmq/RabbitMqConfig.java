package org.xcepto.xceptoj.rabbitmq;

import java.util.HashMap;
import java.util.Map;

public class RabbitMqConfig {
  private final Map<String, RabbitMqExchange> exchanges = new HashMap<>();
  String username = "guest";
  String password = "guest";
  int port = 5672;
  String hostname = "localhost";

  Iterable<RabbitMqExchange> getExchanges() {
    return exchanges.values();
  }

  public void setHostName(String hostname) {
    this.hostname = hostname;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void addExchange(RabbitMqExchange rabbitMqExchange) {
    exchanges.put(rabbitMqExchange.name, rabbitMqExchange);
  }
}