package twila.credicarro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.OccupationType;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroAuthenticationException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.internal.auth.AccessTokenProviderImpl;
import twila.credicarro.internal.resilience.Deadline;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.serialization.CredicarroObjectMapper;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;
import twila.credicarro.webhooks.CredicarroWebhookEvent;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.ProposalEventReason;
import twila.credicarro.webhooks.model.ProposalWebhookEvent;

class SerializationAndTokenErrorsTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);

    @Test
    void jacksonSerializesModelsWithRealValuesMaskingIsOnlyInToString() throws Exception {
        UpsertCustomerRequest request = UpsertCustomerRequest.builder()
                .cpf("12345678909").firstName("Maria").lastName("Souza").email("maria.souza@exemplo.com.br")
                .phoneNumber("+55 (11) 99876-5432").dateOfBirth(OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.UTC))
                .address(CustomerAddress.builder().street("Av. Paulista").postalCode("01311000").city("São Paulo").state("SP").build())
                .professionalData(ProfessionalData.builder().occupation(OccupationType.EMPLOYEE).monthlyIncome(new BigDecimal("6500.00")).build())
                .build();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode json = mapper.readTree(mapper.writeValueAsString(request));

        assertThat(json.get("cpf").asText()).isEqualTo("12345678909");
        assertThat(json.get("email").asText()).isEqualTo("maria.souza@exemplo.com.br");
        assertThat(json.get("professionalData").get("monthlyIncome").decimalValue()).isEqualByComparingTo("6500.00");
        assertThat(request.toString()).doesNotContain("12345678909");

        UpsertCustomerRequest back = CredicarroObjectMapper.DEFAULT.readValue(CredicarroObjectMapper.DEFAULT.writeValueAsString(request), UpsertCustomerRequest.class);
        assertThat(back.getCpf()).isEqualTo("12345678909");
        assertThat(back.getAddress().getCity()).isEqualTo("São Paulo");
    }

    @Test
    void responseModelsRoundTripThroughJacksonWithRealValues() throws Exception {
        server.stubFor(post(urlEqualTo(path("v1/webhook"))).willReturn(jsonResponse(201,
                "{'id_webhook':'" + Fixtures.WEBHOOK_ID + "','segredo_assinatura':'segredo-gerado'}")));

        CreateWebhookResult result = new CreateWebhookResult(UUID.fromString(Fixtures.WEBHOOK_ID), "segredo-gerado");
        String json = CredicarroObjectMapper.DEFAULT.writeValueAsString(result);
        assertThat(json).contains("segredo-gerado");
        assertThat(CredicarroObjectMapper.DEFAULT.readValue(json, CreateWebhookResult.class).getSigningSecret()).isEqualTo("segredo-gerado");
        assertThat(result.toString()).doesNotContain("segredo-gerado");
    }

    @Test
    void proposalDetailsSerializeWithRealCpf() throws Exception {
        server.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID)))
                .willReturn(jsonResponse(200, Fixtures.proposalV2())));

        ProposalDetails details = client.proposals().get(proposalId);
        String json = CredicarroObjectMapper.DEFAULT.writeValueAsString(details);

        assertThat(json).contains("12345678909").contains("sessao/abc123");
        assertThat(details.toString()).doesNotContain("12345678909").doesNotContain("abc123");
    }

    @Test
    void internalObjectsHoldingSecretsRefuseSerialization() {
        ObjectMapper strict = new ObjectMapper();

        assertThatThrownBy(() -> strict.writeValueAsString(client)).isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException.class);
        assertThatThrownBy(() -> strict.writeValueAsString(new AccessTokenProviderImpl((id, secret, deadline) -> null, "id", "segredo")))
                .isInstanceOf(com.fasterxml.jackson.databind.JsonMappingException.class);
    }

    @Test
    void eventReasonIsNullWhenAbsentAndUnknownOnlyForUnrecognizedPresentValues() {
        ProposalWebhookEvent v1 = CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"enum_status\":2}");
        assertThat(v1.getEventReason()).isNull();
        assertThat(v1.getEventReasonRaw()).isNull();

        ProposalWebhookEvent odd = CredicarroWebhookEvent.parseUnverified(
                "{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"enum_status\":2,\"motivo_evento\":\"NOVO_MOTIVO\"}");
        assertThat(odd.getEventReason()).isEqualTo(ProposalEventReason.UNKNOWN);
        assertThat(odd.getEventReasonRaw()).isEqualTo("NOVO_MOTIVO");
    }

    @Test
    void waitingForTheTokenLockNeverOutlivesTheCallDeadline() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl((id, secret, deadline) -> {
            started.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            twila.credicarro.internal.auth.GenerateAccessTokenResponse response = new twila.credicarro.internal.auth.GenerateAccessTokenResponse();
            response.accessToken = "t";
            response.expiresInSeconds = 3600;
            return response;
        }, "id", "secret");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> renewing = pool.submit(() -> provider.getToken(Deadline.after(Duration.ofSeconds(30))));
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

            long start = System.nanoTime();
            assertThatThrownBy(() -> provider.getToken(Deadline.after(Duration.ofMillis(300)))).isInstanceOf(CredicarroTimeoutException.class);
            assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(2000);

            release.countDown();
            assertThat(renewing.get(5, TimeUnit.SECONDS)).isEqualTo("t");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void tokenEndpointErrorsMapPerStatus() {
        for (int status : new int[] {400, 401}) {
            server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(jsonResponse(status, "{'tipo':'Integration.Authentication.InvalidCredentials','titulo':'x'}")));
            assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroAuthenticationException.class);
        }

        for (int status : new int[] {302, 403, 404, 409}) {
            server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(jsonResponse(status, "{'tipo':'Token.Teste','titulo':'x'}")));
            assertThatThrownBy(() -> client.proposals().get(proposalId))
                    .isInstanceOf(CredicarroApiException.class)
                    .satisfies(ex -> {
                        assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(status);
                        assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Token.Teste");
                    });
        }

        server.resetRequests();
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(503)));
        assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(503));
        server.verify(3, postRequestedFor(urlEqualTo(TOKEN_PATH)).withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.containing("clientId")));
    }
}
