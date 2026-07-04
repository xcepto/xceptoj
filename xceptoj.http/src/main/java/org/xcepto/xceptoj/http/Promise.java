package org.xcepto.xceptoj.http;

public class Promise<T> {
  private volatile T value;
  private volatile boolean settled = false;

  public void settle(T value) {
    this.value = value;
    this.settled = true;
  }

  public T resolve() {
    if (!settled) throw new IllegalStateException(
        "Promise not yet settled — resolve() called before the step that settles it has completed");
    return value;
  }
}
