package twila.credicarro.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import twila.credicarro.CredicarroClient;
import twila.credicarro.config.CredicarroResilienceOptions;

/**
 * Base dos testes com WireMock: sobe o servidor, stub do token e cliente apontando para {@code /integration}.
 */
public abstract class ApiTestSupport {

    protected static final String BASE_PATH = "/integration";
    protected static final String TOKEN_PATH = BASE_PATH + "/v1/authentication/accesstoken";
    protected static final String CLIENT_SECRET = "segredo-do-cliente-que-nunca-vaza";

    protected WireMockServer server;
    protected CredicarroClient client;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        server.stubFor(tokenStub("token-de-teste", 3600));
        client = newClient(fastResilience().build());
    }

    @AfterEach
    void stopServer() {
        client.close();
        server.stop();
    }

    protected static CredicarroResilienceOptions.CredicarroResilienceOptionsBuilder fastResilience() {
        return CredicarroResilienceOptions.builder()
                .totalTimeout(Duration.ofSeconds(10))
                .attemptTimeout(Duration.ofSeconds(3))
                .retryBaseDelay(Duration.ofMillis(10));
    }

    /**
     * Base URL sem "/" final de propósito (regressão da resolução de URI relativa, RFC 3986 5.3).
     */
    protected CredicarroClient newClient(CredicarroResilienceOptions resilience) {
        return CredicarroClient.builder()
                .clientId("client-id")
                .clientSecret(CLIENT_SECRET)
                .baseUrl(URI.create("http://localhost:" + server.port() + BASE_PATH))
                .resilience(resilience)
                .build();
    }

    protected static MappingBuilder tokenStub(String token, int expiresInSeconds) {
        return post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(json("{'token_de_acesso':'" + token + "','tipo_de_token':'Bearer','expira_em_segundos':" + expiresInSeconds
                        + ",'escopo':'credicarro.integration.access/full'}")));
    }

    protected static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(int status, String body) {
        return aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(json(body));
    }

    /**
     * Aspas simples viram aspas duplas, para escrever JSON legível nos testes.
     */
    protected static String json(String value) {
        return value.replace('\'', '"');
    }

    protected static String path(String route) {
        return BASE_PATH + "/" + route;
    }
}
