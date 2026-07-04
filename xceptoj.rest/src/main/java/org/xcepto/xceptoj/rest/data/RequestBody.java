package org.xcepto.xceptoj.rest.data;

import java.util.function.Function;
import java.util.function.Supplier;

public class RequestBody {
  private final Supplier<Object> objectSupplier;
  private final Function<Object, String> serialization;

  public RequestBody(Supplier<Object> objectSupplier, Function<Object, String> serialization) {
    this.objectSupplier = objectSupplier;
    this.serialization = serialization;
  }

  public String serialize() {
    return serialization.apply(objectSupplier.get());
  }
}
