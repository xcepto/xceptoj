package com.example.warehouse.inventory;

import com.example.warehouse.events.StockReservedEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class StockReservedProducer {

  private static final String QUEUE_NAME = StockReservedEvent.class.getName();
  Channel channel;
  Gson gson = new Gson();

  public StockReservedProducer(Connection connection) throws IOException {
    channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, "warehouse", QUEUE_NAME);
  }

  public void Send(int amount) throws IOException {
    StockReservedEvent event = new StockReservedEvent();
    event.amount = amount;
    String message = gson.toJson(event);
    channel.basicPublish(InventoryApplication.Exchange, QUEUE_NAME, null, message.getBytes());
  }
}
