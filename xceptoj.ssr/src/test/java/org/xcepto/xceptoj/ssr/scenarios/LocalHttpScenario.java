package org.xcepto.xceptoj.ssr.scenarios;

import com.sun.net.httpserver.HttpServer;
import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalHttpScenario extends Scenario {

  public int port;
  public URI baseUri;
  private HttpServer server;
  private final Map<String, RouteHandler> routes = new HashMap<>();

  @FunctionalInterface
  public interface RouteHandler {
    Response handle(Map<String, String> formFields, Map<String, String> headers, Map<String, String> cookies);
  }

  public record Response(int status, String body, Map<String, String> setCookies) {
    public Response(int status, String body) {
      this(status, body, Map.of());
    }
  }

  public void addRoute(String path, RouteHandler handler) {
    routes.put(path, handler);
  }

  public void addRoute(String path, int status, String responseBody) {
    routes.put(path, (form, headers, cookies) -> new Response(status, responseBody));
  }

  @Override
  public void setupEnvironment() throws XceptoScenarioResetException {
    try {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      port = server.getAddress().getPort();
      baseUri = URI.create("http://localhost:" + port);

      for (var entry : routes.entrySet()) {
        String path = entry.getKey();
        RouteHandler handler = entry.getValue();
        server.createContext(path, exchange -> {
          String rawBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Map<String, String> formFields = parseForm(rawBody);
          Map<String, String> headers = new HashMap<>();
          exchange.getRequestHeaders().forEach((k, v) -> headers.put(k.toLowerCase(), v.get(0)));
          Map<String, String> cookies = parseCookies(headers.getOrDefault("cookie", ""));

          Response response = handler.handle(formFields, headers, cookies);

          byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
          response.setCookies().forEach((name, value) ->
              exchange.getResponseHeaders().add("Set-Cookie", name + "=" + value + "; Path=/"));
          exchange.sendResponseHeaders(response.status(), bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });
      }
      server.start();
    } catch (IOException e) {
      throw new XceptoScenarioResetException("Failed to start local HTTP server", e);
    }
  }

  @Override
  public void stopEnvironment() throws XceptoScenarioResetException {
    if (server != null) server.stop(0);
  }

  private static Map<String, String> parseForm(String body) {
    if (body == null || body.isBlank()) return Map.of();
    return Arrays.stream(body.split("&"))
        .filter(pair -> pair.contains("="))
        .collect(Collectors.toMap(
            pair -> URLDecoder.decode(pair.split("=", 2)[0], StandardCharsets.UTF_8),
            pair -> URLDecoder.decode(pair.split("=", 2)[1], StandardCharsets.UTF_8),
            (a, b) -> a));
  }

  private static Map<String, String> parseCookies(String cookieHeader) {
    if (cookieHeader.isBlank()) return Map.of();
    return Arrays.stream(cookieHeader.split(";"))
        .map(String::trim)
        .filter(pair -> pair.contains("="))
        .collect(Collectors.toMap(
            pair -> pair.split("=", 2)[0].trim(),
            pair -> pair.split("=", 2)[1].trim(),
            (a, b) -> a));
  }
}
