package org.xcepto.xceptoj.rabbitmq;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class RabbitMqRepository {
  private final Map<String, Queue<Object>> messages = new HashMap<>();

  void enqueueMessage(Type type, Object message) {
    var typeFullName = type.getTypeName();
    if (!messages.containsKey(typeFullName)) {
      messages.put(typeFullName, new LinkedBlockingQueue<>());
    }
    Queue<Object> objects = messages.get(typeFullName);
    objects.add(message);
  }

  Optional<Object> dequeueMessage(Class<?> type) {
    var typeName = type.getName();
    if (!messages.containsKey(typeName))
      return Optional.empty();
    var queue = messages.get(typeName);
    if (queue.size() <= 0)
      return Optional.empty();
    Object polled = queue.poll();
    if (polled == null)
      return Optional.empty();
    return Optional.of(polled);
  }
}