package org.xcepto.xceptoj.docs;

import org.junit.jupiter.api.Test;
import org.xcepto.xceptoj.Xcepto;
import org.xcepto.xceptoj.TimeoutConfig;
import org.xcepto.xceptoj.docs.adapters.OrderAdapterExample.OrderAdapter;
import org.xcepto.xceptoj.docs.adapters.OrderAdapterExample.OrderAdapterBuilder;
import org.xcepto.xceptoj.docs.adapters.OrderAdapterExample.OrderStatus;
import org.xcepto.xceptoj.docs.models.AcceptShipmentRequest;
import org.xcepto.xceptoj.docs.models.AcceptShipmentResponse;
import org.xcepto.xceptoj.docs.models.LoginRequest;
import org.xcepto.xceptoj.docs.models.ProfileResponse;
import org.xcepto.xceptoj.docs.models.RegisterRequest;
import org.xcepto.xceptoj.docs.models.StockResponse;
import org.xcepto.xceptoj.docs.models.TokenCreateRequest;
import org.xcepto.xceptoj.docs.models.TokenResponse;
import org.xcepto.xceptoj.docs.scenarios.ShipmentScenario;
import org.xcepto.xceptoj.docs.scenarios.UserScenario;
import org.xcepto.xceptoj.exceptions.XceptoAdapterInitializationException;
import org.xcepto.xceptoj.exceptions.XceptoAdapterTerminationException;
import org.xcepto.xceptoj.exceptions.XceptoScenarioResetException;
import org.xcepto.xceptoj.exceptions.XceptoTestFailedException;
import org.xcepto.xceptoj.http.Promise;
import org.xcepto.xceptoj.rest.builders.RestAdapterBuilder;
import org.xcepto.xceptoj.ssr.builders.SsrAdapterBuilder;
import org.xcepto.xceptoj.ssr.extensions.SsrExtensions;

import java.time.Duration;

public class HomepageSnippets {

    private static final TimeoutConfig TIMEOUT = TimeoutConfig.fromSeconds(30);

    // Helper used in Promise snippet
    private static String extractToken(String page) {
        return page;
    }

    @Test
    public void heroSnippet_Shipment() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        ShipmentScenario scenario = new ShipmentScenario();
        Xcepto.given(scenario, builder -> {
            var rest = new RestAdapterBuilder(builder)
                .withBaseUrl(scenario.apiAddress)
                .withSerializer(new GsonSerializer())
                .build();

            rest.post("/shipment/accept")
                .withRequestBody(() -> new AcceptShipmentRequest(50))
                .withResponseType(AcceptShipmentResponse.class)
                .assertThatResponse(r -> r.amount == 50);

            // Retried until the assertion passes — no sleep needed
            rest.get("/inventory/stock")
                .withResponseType(StockResponse.class)
                .assertThatResponse(r -> r.replenished);
        }, TIMEOUT, Duration.ofMillis(100));
    }

    @Test
    public void stateMachine_ConditionsNotTiming() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        ShipmentScenario scenario = new ShipmentScenario();
        Xcepto.given(scenario, builder -> {
            var rest = new RestAdapterBuilder(builder)
                .withBaseUrl(scenario.apiAddress)
                .withSerializer(new GsonSerializer())
                .build();

            // POST executes once — assertion failure is immediate
            rest.post("/shipment/accept")
                .withRequestBody(() -> new AcceptShipmentRequest(50))
                .withResponseType(AcceptShipmentResponse.class)
                .assertThatResponse((AcceptShipmentResponse r) -> r.amount == 50);

            // GET retries until condition is met or timeout expires
            rest.get("/inventory/stock")
                .withResponseType(StockResponse.class)
                .assertThatResponse(r -> r.replenished);
        }, TIMEOUT, Duration.ofMillis(100));
    }

    @Test
    public void restAdapter_TypedResponseAndBearerToken() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        ShipmentScenario scenario = new ShipmentScenario();
        String username = "user@example.com";
        String password = "password";
        Xcepto.given(scenario, builder -> {
            var rest = new RestAdapterBuilder(builder)
                .withBaseUrl(scenario.apiAddress)
                .withSerializer(new GsonSerializer())
                .build();

            Promise<TokenResponse> token = rest.post("/auth/login")
                .withRequestBody(() -> new LoginRequest(username, password))
                .withResponseType(TokenResponse.class)
                .assertThatResponse(r -> !r.accessToken.isEmpty())
                .promiseResponse();

            rest.get("/api/profile")
                .withBearerTokenClient(() -> token.resolve().accessToken)
                .withResponseType(ProfileResponse.class)
                .assertThatResponse((ProfileResponse r) -> r.username.equals(username));
        }, TIMEOUT, Duration.ofMillis(100));
    }

    @Test
    public void ssrAdapter_CookieAwareSession() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        UserScenario scenario = new UserScenario();
        String username = "user@example.com";
        String password = "password";
        Xcepto.given(scenario, builder -> {
            var ssr = new SsrAdapterBuilder(builder)
                .withBaseUrl(scenario.guiAddress).build();

            ssr.post("/auth/register")
                .withFormContent(SsrExtensions.toForm(
                    new RegisterRequest(username, password)))
                .assertSuccess();

            ssr.post("/auth/login")
                .withFormContent(SsrExtensions.toForm(
                    new LoginRequest(username, password)))
                .assertSuccess();

            // Session cookie carried automatically — no manual wiring
            ssr.get("/dashboard")
                .assertThatResponseContentString(html -> html.contains(username));
        }, TIMEOUT, Duration.ofMillis(100));
    }

    @Test
    public void promise_PassDataBetweenSteps() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        UserScenario scenario = new UserScenario();
        Xcepto.given(scenario, builder -> {
            var rest = new RestAdapterBuilder(builder)
                .withBaseUrl(scenario.baseUri)
                .withSerializer(new GsonSerializer())
                .build();

            var ssr = new SsrAdapterBuilder(builder)
                .withBaseUrl(scenario.guiAddress).build();

            // A token is embedded in the rendered HTML page
            Promise<String> page = ssr.post("/token/create")
                .withFormContent(SsrExtensions.toForm(
                    new TokenCreateRequest("deploy-key")))
                .assertSuccess()
                .promiseResponse();

            // REST call resolves the token lazily at execution time
            rest.post("/api/env/create")
                .withBearerTokenClient(() -> extractToken(page.resolve()))
                .assertSuccess();
        }, TIMEOUT, Duration.ofMillis(100));
    }

    @Test
    public void customAdapter_DomainDsl() throws XceptoScenarioResetException, XceptoAdapterInitializationException,
            XceptoTestFailedException, XceptoAdapterTerminationException {
        ShipmentScenario scenario = new ShipmentScenario();
        Xcepto.given(scenario, builder -> {
            // In a test — reads as domain behavior
            var orders = new OrderAdapterBuilder(builder).build();

            orders.order("order-42")
                .withAmount(100)
                .shouldReachStatus(OrderStatus.FULFILLED);
        }, TIMEOUT, Duration.ofMillis(100));
    }
}
