package org.xcepto.xceptoj.rest;

public class Promise<T> {
  private volatile T value;
  private volatile boolean settled = false;

  public void settle(T value) {
    this.value = value;
    this.settled = true;
  }

  public T resolve() {
    if (!settled)
      throw new IllegalStateException(
          "Promise not yet settled; ensure the step that settles this promise runs before the step that uses it");
    return value;
  }
}
