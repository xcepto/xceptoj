package com.example.warehouse.inbound;

import org.testcontainers.containers.ComposeContainer;
import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InboundFlowScenario extends Scenario {

  private ComposeContainer environment;

  @Override
  public void startEnvironment() throws XceptoScenarioResetException {

    try{
      Path projectRoot = Paths.get("").toAbsolutePath().getParent();
      File composeFile = projectRoot.resolve("docker-compose.test.yaml").toFile();
      environment = new ComposeContainer(composeFile)
          .withExposedService("rabbitmq", 5672)
          .withExposedService("postgres", 5432)
          .withExposedService("examples.warehouse.inbound", 8081)
          .withExposedService("examples.warehouse.order", 8080)
          .withExposedService("examples.warehouse.stocktake", 8082)
          .withLocalCompose(true);

      environment.start();
    } catch (Exception e) {
      throw new XceptoScenarioResetException(e.getCause().toString());
    }
  }

  @Override
  public void stopEnvironment() throws XceptoScenarioResetException {
    try{
      environment.stop();
    } catch (Exception e) {
      throw new XceptoScenarioResetException(e.getCause().toString());
    }
  }

  @Override
  public int getPort(String service, int port) {
    return environment.getServicePort(service, port);
  }
}