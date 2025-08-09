package com.example.warehouse.shipping;

import com.example.warehouse.events.StockReservedEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StockReservedConsumer {

  private static final String QUEUE_NAME = StockReservedEvent.class.getName();

  DeliverCallback callback;
  Channel channel;

  public StockReservedConsumer(Connection connection) throws IOException {
    ShipmentSendProducer producer = new ShipmentSendProducer(connection);

    channel = connection.createChannel();
    channel.basicQos(1);
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, ShippingApplication.EXCHANGE, QUEUE_NAME);

    Gson gson = new Gson();

    callback = (tag, delivery) -> {
      try {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

        StockReservedEvent event = gson.fromJson(message, StockReservedEvent.class);

        System.out.printf("Shipment created, amount: %s%n", event.amount);

        producer.Send(event.amount);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      } catch (Exception e) {
        e.printStackTrace();
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
      }
    };
  }

  public void start() throws IOException {
    channel.basicConsume(QUEUE_NAME, false, callback, consumerTag -> {
    });
  }


}
