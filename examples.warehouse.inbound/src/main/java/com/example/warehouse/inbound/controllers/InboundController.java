package com.example.warehouse.inbound.controllers;

import com.example.warehouse.events.ShipmentArrivedEvent;
import com.example.warehouse.inbound.InboundApplication;
import com.example.warehouse.inbound.requests.AcceptShipmentRequest;
import com.example.warehouse.inbound.requests.AcceptShipmentResponse;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InboundController {

  private final RabbitTemplate rabbitTemplate;
  Gson gson = new Gson();

  public InboundController(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @PostMapping("/shipment/accept")
  public AcceptShipmentResponse createItem(@RequestBody AcceptShipmentRequest request) {

    ShipmentArrivedEvent event = new ShipmentArrivedEvent();
    event.amount = request.amount;

    rabbitTemplate.convertAndSend(InboundApplication.shipmentArrivedQueue, gson.toJson(event));
    AcceptShipmentResponse response = new AcceptShipmentResponse();
    response.amount = request.amount;
    return response;
  }

}
