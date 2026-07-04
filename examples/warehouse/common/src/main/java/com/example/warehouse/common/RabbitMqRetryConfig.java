package com.example.warehouse.common;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;

public class RabbitMqRetryConfig {
  public static RetryConfig getRetryConfig() {
    return RetryConfig.custom()
        .maxAttempts(7)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2.0))
        .build();
  }
}
