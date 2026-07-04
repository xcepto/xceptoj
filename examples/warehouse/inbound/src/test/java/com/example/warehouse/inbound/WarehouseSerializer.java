package com.example.warehouse.inbound;

import com.google.gson.Gson;
import org.xcepto.xceptoj.rest.Serializer;

class WarehouseSerializer implements Serializer {
  private final Gson gson = new Gson();

  @Override
  public String serialize(Object obj) {
    return gson.toJson(obj);
  }

  @Override
  public <T> T deserialize(String json, Class<T> type) {
    return gson.fromJson(json, type);
  }
}
