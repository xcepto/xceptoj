package org.xcepto.xceptoj.ssr.extensions;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SsrExtensions {

  private SsrExtensions() {}

  /**
   * Converts a record or POJO to a form-encoded map using reflection, mirroring .NET's ToForm() extension.
   * Field names become form keys; values are converted via toString().
   */
  public static Map<String, String> toForm(Object obj) {
    Map<String, String> map = new LinkedHashMap<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(obj);
        map.put(field.getName(), value != null ? value.toString() : "");
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Cannot access field: " + field.getName(), e);
      }
    }
    return map;
  }
}
