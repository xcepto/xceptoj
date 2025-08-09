package com.example.warehouse.order.controller;

import com.example.warehouse.events.OrderPlacedEvent;
import com.example.warehouse.order.OrderApplication;
import com.example.warehouse.order.requests.PlaceOrderRequest;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  private final RabbitTemplate rabbitTemplate;
  Gson gson = new Gson();

  public OrderController(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @PostMapping("/order/place")
  public HttpStatusCode createItem(@RequestBody PlaceOrderRequest request) {

    OrderPlacedEvent event = new OrderPlacedEvent();
    event.amount = request.amount;

    rabbitTemplate.convertAndSend(OrderApplication.orderPlacedQueue, gson.toJson(event));
    return HttpStatus.OK;
  }

}
