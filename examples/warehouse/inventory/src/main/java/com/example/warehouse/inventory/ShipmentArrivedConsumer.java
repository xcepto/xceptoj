package com.example.warehouse.inventory;

import com.example.warehouse.events.ShipmentArrivedEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class ShipmentArrivedConsumer {

  private static final String QUEUE_NAME = ShipmentArrivedEvent.class.getName();
  private final java.sql.Connection postgresConnection;

  DeliverCallback callback;
  Channel channel;

  public ShipmentArrivedConsumer(Connection connection, PostgresConnectionFactory postgresConnectionFactory) throws IOException, SQLException {
    postgresConnection = postgresConnectionFactory.Build();
    StockReplenishedProducer stockReplenishedProducer = new StockReplenishedProducer(connection);

    channel = connection.createChannel();
    channel.basicQos(1);
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, "warehouse", QUEUE_NAME);

    Gson gson = new Gson();

    callback = (tag, delivery) -> {
      String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

      ShipmentArrivedEvent event = gson.fromJson(message, ShipmentArrivedEvent.class);

      try {
        postgresConnection.setAutoCommit(false);
        String insertSql = "INSERT INTO inventory (change, created) VALUES (?, ?)";
        PreparedStatement changeInsertion = postgresConnection.prepareStatement(insertSql);
        changeInsertion.setInt(1, event.amount);
        changeInsertion.setTimestamp(2, Timestamp.from(Instant.now()));
        changeInsertion.execute();


        postgresConnection.commit();
        stockReplenishedProducer.Send(event.amount);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        System.out.printf("Replenished %d stock%n", event.amount);

      } catch (SQLException | InventoryException | IOException e) {
        try {
          postgresConnection.rollback();
        } catch (SQLException ex) {
          System.out.println("Rollback failed: "+ ex.getCause() + ", message: " + ex.getMessage());
          channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
          throw new RuntimeException(ex);
        }
        System.out.println("Failed to run sql transaction: "+ e.getCause() + ", message: " + e.getMessage());
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
      }
    };
  }

  public void start() throws IOException {
    channel.basicConsume(QUEUE_NAME, false, callback, consumerTag -> {
    });
  }


}
