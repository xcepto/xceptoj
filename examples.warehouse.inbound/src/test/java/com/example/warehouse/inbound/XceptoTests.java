package com.example.warehouse.inbound;

import com.example.warehouse.events.StockReplenishedEvent;
import com.example.warehouse.inbound.config.WarehouseRabbitMqConfig;
import com.example.warehouse.inbound.requests.AcceptShipmentRequest;
import com.example.warehouse.inbound.requests.AcceptShipmentResponse;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rabbitmq.RabbitMqXceptoAdapter;
import org.xcepto.xceptoj.rest.RestXceptoAdapter;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XceptoTests {

  @Test
  public void smallShipmentReplenishesStock() throws XceptoScenarioResetException, XceptoAdapterInitializationException, XceptoTestFailedException, XceptoAdapterTerminationException {

    InboundFlowScenario scenario = new InboundFlowScenario();

    // Given
    Xcepto.Given(scenario, builder -> {
      RabbitMqXceptoAdapter rabbitmq = builder.RegisterAdapter(
          new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(
              scenario.getPort("rabbitmq", 5672))));
      RestXceptoAdapter rest = builder.RegisterAdapter(new RestXceptoAdapter());

      // Arrange
      URI acceptUrl = URI.create("http://localhost:%d/shipment/accept".formatted(
          scenario.getPort("examples.warehouse.inbound", 8081)));
      AcceptShipmentRequest request = new AcceptShipmentRequest();
      request.amount = 50;

      // When
      rest.postRequest(acceptUrl, request, AcceptShipmentResponse.class, response -> response.amount == request.amount);

      // Then
      rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total == request.amount);
    }, Duration.ofSeconds(30), Duration.ofMillis(100));
  }

  @Test
  public void largeShipmentReplenishesStock() throws XceptoScenarioResetException, XceptoAdapterInitializationException, XceptoTestFailedException, XceptoAdapterTerminationException {
    InboundFlowScenario scenario = new InboundFlowScenario();
    Xcepto.Given(scenario, builder -> {
      RabbitMqXceptoAdapter rabbitmq = builder.RegisterAdapter(
          new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(
              scenario.getPort("rabbitmq", 5672))));
      RestXceptoAdapter rest = builder.RegisterAdapter(new RestXceptoAdapter());

      URI url = URI.create("http://localhost:%d/shipment/accept".formatted(
          scenario.getPort("examples.warehouse.inbound", 8081)));
      AcceptShipmentRequest request = new AcceptShipmentRequest();
      request.amount = 1500;
      URI stockTakeUrl = URI.create("http://localhost:%d/".formatted(
          scenario.getPort("examples.warehouse.stocktake", 8082)));

      rest.postRequest(url, request, AcceptShipmentResponse.class, response -> response.amount > 1000);
      rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total < 2000);

      // Then check Stocktake HTML
      rest.getHtmlCondition(stockTakeUrl, html -> {
        System.out.println("Received html: "+ html);
        Pattern pattern = Pattern.compile(".*Total: <span>([0-9]*)</span>.*");
        Matcher matcher = pattern.matcher(html);
        if(matcher.find()){
          String total = matcher.group(1);
          try{
            int totalInt = Integer.parseInt(total);
            boolean check = totalInt == request.amount;
            if(!check)
              System.out.println("Total was not as expected: "+ totalInt);
            return check;
          } catch (NumberFormatException e) {
            throw new RuntimeException(e);
          }
        }
        System.out.println("Pattern was not matched in html: "+ html);
        return false;
      });
    }, Duration.ofSeconds(30), Duration.ofMillis(100));
  }
}
