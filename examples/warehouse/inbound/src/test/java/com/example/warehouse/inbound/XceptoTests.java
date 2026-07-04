package com.example.warehouse.inbound;

import com.example.warehouse.events.StockReplenishedEvent;
import com.example.warehouse.inbound.config.WarehouseRabbitMqConfig;
import com.example.warehouse.inbound.requests.AcceptShipmentRequest;
import com.example.warehouse.inbound.requests.AcceptShipmentResponse;
import org.junit.jupiter.api.Disabled;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.rabbitmq.RabbitMqXceptoAdapter;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XceptoTests {

  @Test
  @Disabled("RabbitMQ adapter is being deprecated")
  public void smallShipmentReplenishesStock() throws XceptoScenarioResetException, XceptoAdapterInitializationException, XceptoTestFailedException, XceptoAdapterTerminationException {

    InboundFlowScenario scenario = new InboundFlowScenario();

    Xcepto.given(scenario, builder -> {
      RabbitMqXceptoAdapter rabbitmq = builder.registerAdapter(
          new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(
              scenario.getPort("rabbitmq", 5672))));

      URI inboundUri = URI.create("http://localhost:%d".formatted(
          scenario.getPort("examples.warehouse.inbound", 8081)));
      var rest = new RestAdapterBuilder(builder)
          .withBaseUrl(inboundUri)
          .withSerializer(new WarehouseSerializer())
          .build();

      AcceptShipmentRequest request = new AcceptShipmentRequest();
      request.amount = 50;

      rest.post("/shipment/accept")
          .withRequestBody(() -> request)
          .withResponseType(AcceptShipmentResponse.class)
          .assertThatResponse((AcceptShipmentResponse r) -> r.amount == request.amount);

      rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total == request.amount);
    }, Duration.ofSeconds(30), Duration.ofMillis(100));
  }

  @Test
  @Disabled("RabbitMQ adapter is being deprecated")
  public void largeShipmentReplenishesStock() throws XceptoScenarioResetException, XceptoAdapterInitializationException, XceptoTestFailedException, XceptoAdapterTerminationException {
    InboundFlowScenario scenario = new InboundFlowScenario();

    Xcepto.given(scenario, builder -> {
      RabbitMqXceptoAdapter rabbitmq = builder.registerAdapter(
          new RabbitMqXceptoAdapter(WarehouseRabbitMqConfig.getConfig(
              scenario.getPort("rabbitmq", 5672))));

      URI inboundUri = URI.create("http://localhost:%d".formatted(
          scenario.getPort("examples.warehouse.inbound", 8081)));
      URI stocktakeUri = URI.create("http://localhost:%d".formatted(
          scenario.getPort("examples.warehouse.stocktake", 8082)));

      var inboundRest = new RestAdapterBuilder(builder)
          .withBaseUrl(inboundUri)
          .withSerializer(new WarehouseSerializer())
          .build();
      var stocktakeRest = new RestAdapterBuilder(builder)
          .withBaseUrl(stocktakeUri)
          .build();

      AcceptShipmentRequest request = new AcceptShipmentRequest();
      request.amount = 1500;

      inboundRest.post("/shipment/accept")
          .withRequestBody(() -> request)
          .withResponseType(AcceptShipmentResponse.class)
          .assertThatResponse((AcceptShipmentResponse r) -> r.amount > 1000);

      rabbitmq.eventCondition(StockReplenishedEvent.class, e -> e.total < 2000);

      stocktakeRest.get("/")
          .assertThatResponseContentString(html -> {
            Pattern pattern = Pattern.compile(".*Total: <span>([0-9]*)</span>.*");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
              try {
                int totalInt = Integer.parseInt(matcher.group(1));
                return totalInt == request.amount;
              } catch (NumberFormatException e) {
                return false;
              }
            }
            return false;
          });
    }, Duration.ofSeconds(30), Duration.ofMillis(100));
  }
}
