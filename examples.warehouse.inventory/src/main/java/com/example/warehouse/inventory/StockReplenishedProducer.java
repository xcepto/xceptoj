package com.example.warehouse.inventory;

import com.example.warehouse.events.StockReplenishedEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class StockReplenishedProducer {

  private static final String QUEUE_NAME = StockReplenishedEvent.class.getName();
  Channel channel;
  Gson gson = new Gson();

  public StockReplenishedProducer(Connection connection) throws IOException {
    channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, "warehouse", QUEUE_NAME);
  }

  public void Send(int total) throws IOException {
    StockReplenishedEvent event = new StockReplenishedEvent();
    event.total = total;
    String message = gson.toJson(event);
    channel.basicPublish(InventoryApplication.Exchange, QUEUE_NAME, null, message.getBytes());
  }
}
