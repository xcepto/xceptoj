package org.xcepto.xceptoj.rest.scenarios;

import com.sun.net.httpserver.HttpServer;
import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LocalHttpScenario extends Scenario {
  public int port;
  public URI baseUri;
  private HttpServer server;
  private final Map<String, RouteHandler> routes = new HashMap<>();

  @FunctionalInterface
  public interface RouteHandler {
    Response handle(String requestBody, Map<String, String> headers);
  }

  public record Response(int status, String body) {}

  public void addRoute(String path, RouteHandler handler) {
    routes.put(path, handler);
  }

  public void addRoute(String path, int status, String responseBody) {
    routes.put(path, (body, headers) -> new Response(status, responseBody));
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
          String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Map<String, String> headers = new HashMap<>();
          exchange.getRequestHeaders().forEach((k, v) -> headers.put(k.toLowerCase(), v.get(0)));
          Response response = handler.handle(requestBody, headers);
          byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
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
}
