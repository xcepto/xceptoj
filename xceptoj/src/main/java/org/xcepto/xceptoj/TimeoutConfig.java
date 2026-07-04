package org.xcepto.xceptoj;

import java.time.Duration;

public class TimeoutConfig {
  private final Duration total;
  private final Duration test;

  public TimeoutConfig(Duration total, Duration test) {
    this.total = total;
    this.test = test;
  }

  public static TimeoutConfig fromSeconds(float totalSeconds) {
    Duration total = Duration.ofMillis((long) (totalSeconds * 1000));
    Duration test = totalSeconds >= 1
        ? Duration.ofMillis((long) ((totalSeconds - 0.5f) * 1000))
        : total;
    return new TimeoutConfig(total, test);
  }

  public static TimeoutConfig fromSeconds(float totalSeconds, float testSeconds) {
    return new TimeoutConfig(
        Duration.ofMillis((long) (totalSeconds * 1000)),
        Duration.ofMillis((long) (testSeconds * 1000)));
  }

  public Duration getTotal() {
    return total;
  }

  public Duration getTest() {
    return test;
  }
}