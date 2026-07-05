package org.xcepto.xceptoj.docs.scenarios;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.xcepto.xceptoj.Scenario;
import org.xcepto.xceptoj.docs.DocsApiImageSingleton;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;

import java.net.URI;
import java.time.Duration;

public class ShipmentScenario extends Scenario {

    private GenericContainer<?> container;
    public URI apiAddress;

    @Override
    public void startEnvironment() throws XceptoScenarioResetException {
        try {
            container = new GenericContainer<>(DocsApiImageSingleton.getImage())
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/api/ping")
                            .forPort(8080)
                            .withStartupTimeout(Duration.ofSeconds(120)));
            container.start();
            apiAddress = URI.create("http://localhost:" + container.getMappedPort(8080));
        } catch (Exception e) {
            throw new XceptoScenarioResetException(e.getMessage());
        }
    }

    @Override
    public void stopEnvironment() throws XceptoScenarioResetException {
        if (container != null) {
            container.stop();
        }
    }
}
