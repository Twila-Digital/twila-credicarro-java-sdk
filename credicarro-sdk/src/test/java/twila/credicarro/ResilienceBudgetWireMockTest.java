package twila.credicarro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroRateLimitException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.proposals.model.DocumentFile;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

/**
 * Orçamento de tempo, Retry-After, redirects e retry do token.
 */
class ResilienceBudgetWireMockTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);
    private final String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);

    @Test
    void mutedServerRespectsTheTotalBudgetWithShortTimes() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withFixedDelay(6000)));

        try (CredicarroClient impatient = newClient(fastResilience().totalTimeout(Duration.ofSeconds(2)).attemptTimeout(Duration.ofSeconds(2)).build())) {
            long start = System.nanoTime();

            assertThatThrownBy(() -> impatient.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);

            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            assertThat(elapsedMillis).isGreaterThan(1500).isLessThan(4500);
        }
    }

    @Test
    void attemptsNeverOutliveTheRemainingBudgetAcrossRetries() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200).withFixedDelay(6000)));

        try (CredicarroClient impatient = newClient(fastResilience().totalTimeout(Duration.ofSeconds(2)).attemptTimeout(Duration.ofMillis(700))
                .retryBaseDelay(Duration.ofMillis(50)).build())) {
            long start = System.nanoTime();

            assertThatThrownBy(() -> impatient.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);

            assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(4000);
        }
    }

    @Test
    void attemptTimeoutLongerThanTheHttpLibraryDefaultIsHonoured() {
        // O OkHttp trazia 10 s de read timeout; com a tentativa em 20 s uma resposta de 11 s precisa chegar.
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(200, Fixtures.proposalV2()).withFixedDelay(11000)));

        try (CredicarroClient patient = newClient(fastResilience().totalTimeout(Duration.ofSeconds(25)).attemptTimeout(Duration.ofSeconds(20)).build())) {
            assertThat(patient.proposals().get(proposalId).getId()).isEqualTo(proposalId);
        }
    }

    @Test
    void documentUploadBudgetIsNotCutByTheShortTotalTimeout() {
        String uploadUrl = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        server.stubFor(post(urlEqualTo(uploadUrl)).willReturn(aResponse().withStatus(204).withFixedDelay(1500)));

        try (CredicarroClient client2 = newClient(fastResilience().totalTimeout(Duration.ofSeconds(1)).attemptTimeout(Duration.ofSeconds(1))
                .documentUploadAttemptTimeout(Duration.ofSeconds(5)).build())) {
            client2.proposals().uploadDocument(proposalId, ProposalDocumentUpload.builder().participant(ProposalParticipant.BUYER)
                    .documentType(DocumentType.CNH).file(DocumentFile.fromBytes(new byte[] {1}, "cnh.pdf")).build());
        }

        server.verify(1, postRequestedFor(urlEqualTo(uploadUrl)));
    }

    @Test
    void retryAfterLargerThanTheRemainingBudgetFailsImmediatelyWithRateLimit() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120")));

        long start = System.nanoTime();

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroRateLimitException.class)
                .satisfies(ex -> assertThat(((CredicarroRateLimitException) ex).getRetryAfter()).isEqualTo(Duration.ofSeconds(120)));

        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(2000);
        server.verify(1, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void retryAfterAsAnHttpDateIsHonoured() {
        String date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(2));
        server.stubFor(get(urlEqualTo(url)).inScenario("data").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", date)).willSetStateTo("ok"));
        server.stubFor(get(urlEqualTo(url)).inScenario("data").whenScenarioStateIs("ok").willReturn(jsonResponse(200, Fixtures.proposalV2())));

        long start = System.nanoTime();

        assertThat(client.proposals().get(proposalId)).isNotNull();

        assertThat((System.nanoTime() - start) / 1_000_000).isGreaterThan(500);
        server.verify(2, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void invalidZeroOrNegativeRetryAfterFallsBackToTheNormalBackoff() {
        for (String value : new String[] {"abc", "-5", "0", ""}) {
            server.resetAll();
            server.stubFor(tokenStub("token-de-teste", 3600));
            server.stubFor(get(urlEqualTo(url)).inScenario("ra").whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(503).withHeader("Retry-After", value)).willSetStateTo("ok"));
            server.stubFor(get(urlEqualTo(url)).inScenario("ra").whenScenarioStateIs("ok").willReturn(jsonResponse(200, Fixtures.proposalV2())));

            assertThat(client.proposals().get(proposalId)).as(value).isNotNull();
            server.verify(2, getRequestedFor(urlEqualTo(url)));
        }
    }

    @Test
    void redirectIsNotFollowedAndBecomesAnApiException() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(302).withHeader("Location", "http://localhost:1/outro")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(302));

        server.verify(1, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void redirectOnTheTokenEndpointBecomesAnApiException() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(307).withHeader("Location", "http://localhost:1/token")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(307));

        server.verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void tokenEndpointRetriesTransientStatusesThenSucceeds() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).inScenario("token").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503)).willSetStateTo("2"));
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).inScenario("token").whenScenarioStateIs("2")
                .willReturn(aResponse().withStatus(502)).willSetStateTo("3"));
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).inScenario("token").whenScenarioStateIs("3")
                .willReturn(jsonResponse(200, "{'token_de_acesso':'t','tipo_de_token':'Bearer','expira_em_segundos':3600}")));
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        assertThat(client.proposals().get(proposalId)).isNotNull();

        server.verify(3, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void tokenNetworkFailureBecomesATimeoutNotAnAuthenticationError() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);
    }

    @Test
    void networkFailureCauseIsSanitized() {
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroTimeoutException.class)
                .hasMessageContaining("rede ou timeout")
                .satisfies(ex -> {
                    Throwable cause = ex.getCause();
                    assertThat(cause).isNotNull();
                    assertThat(cause.getCause()).isNull();
                    assertThat(cause.getClass().getName()).doesNotContain("okhttp");
                    assertThat(String.valueOf(cause.getMessage())).doesNotContain("localhost").doesNotContain(String.valueOf(server.port()));
                });
    }

    @Test
    void noBackgroundThreadsAreCreatedPerCall() {
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        for (int i = 0; i < 5; i++)
            client.proposals().get(proposalId);

        boolean leaked = Thread.getAllStackTraces().keySet().stream().anyMatch(thread -> thread.getName().startsWith("credicarro-resilience"));
        assertThat(leaked).isFalse();
    }
}
