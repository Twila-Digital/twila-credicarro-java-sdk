package twila.credicarro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.errors.CredicarroRateLimitException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.errors.CredicarroValidationException;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

/**
 * Do HTTP até a exceção tipada, passando pela pipeline completa.
 */
class ErrorMappingWireMockTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);
    private final String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);

    @Test
    void unprocessableEntityBecomesABusinessRuleException() {
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(422,
                "{'tipo':'Proposal.Access.NotIntegrationV2','titulo':'Regra de negócio não atendida','status':422,'detalhe':'Proposta de outro fluxo.'}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> {
                    CredicarroBusinessRuleException api = (CredicarroBusinessRuleException) ex;
                    assertThat(api.getStatusCode()).isEqualTo(422);
                    assertThat(api.getErrorCode()).isEqualTo("Proposal.Access.NotIntegrationV2");
                    assertThat(api.getDetail()).isEqualTo("Proposta de outro fluxo.");
                });
    }

    @Test
    void badRequestBecomesAValidationExceptionWithTheField() {
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(400,
                "{'tipo':'Validation.Identifier.Invalid','titulo':'Requisição inválida','status':400,'detalhe':'Identificador inválido.','campo':'propostaId'}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroValidationException.class)
                .satisfies(ex -> assertThat(((CredicarroValidationException) ex).getField()).isEqualTo("propostaId"));
    }

    @Test
    void standardProblemDetailsOnAnUnexpectedFailureFallBackToTypeTitleDetail() {
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(500,
                "{'type':'https://tools.ietf.org/html/rfc9110#section-15.6.1','title':'Erro inesperado','status':500,'detail':'falha interna'}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> {
                    CredicarroApiException api = (CredicarroApiException) ex;
                    assertThat(api.getStatusCode()).isEqualTo(500);
                    assertThat(api.getErrorCode()).startsWith("https://tools.ietf.org");
                    assertThat(api.getTitle()).isEqualTo("Erro inesperado");
                    assertThat(api.getDetail()).isEqualTo("falha interna");
                    assertThat(api.getMessage()).doesNotContain("falha interna");
                });

        // 500 é repetido por padrão (RetryOn500 = true): 3 tentativas.
        server.verify(3, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void tooManyRequestsWithAnIgnoredZeroRetryAfterBecomesARateLimitExceptionAfterTheRetriesRunOut() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroRateLimitException.class)
                .satisfies(ex -> assertThat(((CredicarroRateLimitException) ex).getRetryAfter()).isNull());
    }

    @Test
    void errorWithoutAJsonBodyStillBecomesAnApiException() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(404).withBody("<html>nao encontrado</html>")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> {
                    CredicarroApiException api = (CredicarroApiException) ex;
                    assertThat(api.getStatusCode()).isEqualTo(404);
                    assertThat(api.getErrorCode()).isNull();
                    assertThat(api.getRawBody()).contains("nao encontrado");
                });
    }

    @Test
    void attemptTimeoutBecomesATimeoutException() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withFixedDelay(1500)));

        try (CredicarroClient impatient = newClient(fastResilience()
                .attemptTimeout(Duration.ofMillis(200)).maxRetryAttempts(1).build())) {
            assertThatThrownBy(() -> impatient.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);
        }
    }

    @Test
    void totalTimeoutBecomesATimeoutException() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withFixedDelay(3000)));

        try (CredicarroClient impatient = newClient(fastResilience()
                .totalTimeout(Duration.ofMillis(300)).attemptTimeout(Duration.ofMillis(300)).maxRetryAttempts(1).build())) {
            assertThatThrownBy(() -> impatient.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);
        }
    }

    @Test
    void circuitBreakerOpensAfterConsistentTransientFailures() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(503)));

        try (CredicarroClient fragile = newClient(fastResilience().maxRetryAttempts(1)
                .circuitBreakerMinimumThroughput(2).circuitBreakerSlidingWindowSize(4).circuitBreakerFailureRatio(0.5).build())) {
            for (int i = 0; i < 2; i++)
                assertThatThrownBy(() -> fragile.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class);

            assertThatThrownBy(() -> fragile.proposals().get(proposalId))
                    .isInstanceOf(CredicarroTimeoutException.class)
                    .hasMessageContaining("circuit breaker");
        }

        server.verify(2, getRequestedFor(urlEqualTo(url)));
    }
}
