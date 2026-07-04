package org.xcepto.xceptoj.rest;

public interface Serializer {
  String serialize(Object obj);
  <T> T deserialize(String json, Class<T> type);
}
