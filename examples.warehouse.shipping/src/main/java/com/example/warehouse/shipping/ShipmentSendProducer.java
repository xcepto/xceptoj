package com.example.warehouse.shipping;

import com.example.warehouse.events.ShipmentSendEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;

public class ShipmentSendProducer {

  private static final String QUEUE_NAME = ShipmentSendEvent.class.getName();
  Channel channel;
  Gson gson = new Gson();

  public ShipmentSendProducer(Connection connection) throws IOException {
    channel = connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, ShippingApplication.EXCHANGE, QUEUE_NAME);
  }

  public void Send(int amount) throws IOException {
    ShipmentSendEvent event = new ShipmentSendEvent();
    event.amount = amount;
    String message = gson.toJson(event);
    channel.basicPublish(ShippingApplication.EXCHANGE, QUEUE_NAME, null, message.getBytes());
    System.out.println("✅ Sent: " + message);
  }
}
