package com.example.warehouse.inventory;

import com.example.warehouse.events.OrderPlacedEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class OrderPlacedConsumer {

  private static final String QUEUE_NAME = OrderPlacedEvent.class.getName();
  private final java.sql.Connection postgresConnection;

  DeliverCallback callback;
  Channel channel;

  public OrderPlacedConsumer(Connection connection, PostgresConnectionFactory postgresConnectionFactory) throws IOException, SQLException {
    StockReservedProducer stockReservedProducer = new StockReservedProducer(connection);
    postgresConnection = postgresConnectionFactory.Build();

    channel = connection.createChannel();
    channel.basicQos(1);
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.queueBind(QUEUE_NAME, "warehouse", QUEUE_NAME);

    Gson gson = new Gson();

    callback = (tag, delivery) -> {
      String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

      OrderPlacedEvent event = gson.fromJson(message, OrderPlacedEvent.class);

      try {
        postgresConnection.setAutoCommit(false);
        String selectSql = "SELECT SUM(change) as total FROM inventory";
        PreparedStatement totalQuery = postgresConnection.prepareStatement(selectSql);
        ResultSet resultSet = totalQuery.executeQuery();
        if (!resultSet.next())
          throw new InventoryException("Failed to Sum total on inventory");
        int total = resultSet.getInt("total");
        if(total < event.amount)
          throw new InventoryException("Not enough stock: total ("+ total+ ") is lower than ("+ event.amount+ ")!");
        String insertSql = "INSERT INTO inventory (change, created) VALUES (?, ?)";
        PreparedStatement changeInsertion = postgresConnection.prepareStatement(insertSql);
        changeInsertion.setInt(1, -event.amount);
        changeInsertion.setTimestamp(2, Timestamp.from(Instant.now()));
        changeInsertion.execute();


        postgresConnection.commit();
        stockReservedProducer.Send(event.amount);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        System.out.printf("Reserved %d stock, new total: %d%n", event.amount, total - event.amount);

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
