package org.xcepto.xceptoj.rabbitmq;

import org.xcepto.xceptoj.XceptoState;
import org.xcepto.xceptoj.interfaces.ServiceProvider;

import java.util.function.Predicate;

public class RabbitMqXceptoState extends XceptoState {
  private final Predicate<ServiceProvider> validation;

  public RabbitMqXceptoState(String name, Predicate<ServiceProvider> validation) {
    super(name);
    this.validation = validation;
  }

  @Override
  public boolean evaluateConditionsForTransition(ServiceProvider serviceProvider) {
    return validation.test(serviceProvider);
  }

  @Override
  public void onEnter(ServiceProvider serviceProvider) {
  }
}
