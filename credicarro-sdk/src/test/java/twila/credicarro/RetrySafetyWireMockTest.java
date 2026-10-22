package twila.credicarro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.customers.model.CreateCustomerRequest;
import twila.credicarro.customers.model.CustomerAddress;
import twila.credicarro.customers.model.OccupationType;
import twila.credicarro.customers.model.ProfessionalData;
import twila.credicarro.customers.model.UpsertCustomerRequest;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.proposals.model.DocumentFile;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.proposals.model.VehicleDetails;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookType;

/**
 * Regras de retry e Idempotency-Key do contrato: o que é repetido, o que nunca é, e que a chave é a mesma em todas as tentativas.
 */
class RetrySafetyWireMockTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);
    private final UUID webhookId = UUID.fromString(Fixtures.WEBHOOK_ID);

    private static UpsertCustomerRequest unifiedRequest() {
        return UpsertCustomerRequest.builder()
                .cpf("12345678909").firstName("Maria").lastName("Souza").email("maria.souza@exemplo.com.br")
                .phoneNumber("+55 (11) 99876-5432")
                .dateOfBirth(OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-3)))
                .address(CustomerAddress.builder().street("Av. Paulista").neighborhood("Bela Vista").city("São Paulo")
                        .state("SP").postalCode("01311000").build())
                .professionalData(ProfessionalData.builder().occupation(OccupationType.EMPLOYEE).monthlyIncome(new BigDecimal("6500.00")).build())
                .build();
    }

    private static CreateCustomerRequest createRequest() {
        return CreateCustomerRequest.builder()
                .cpf("12345678909").firstName("Maria").lastName("Souza").email("maria.souza@exemplo.com.br")
                .phoneNumber("+55 (11) 99876-5432")
                .dateOfBirth(OffsetDateTime.of(1990, 5, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-3)))
                .build();
    }

    private static ProposalDocumentUpload upload() {
        return ProposalDocumentUpload.builder()
                .participant(ProposalParticipant.BUYER)
                .documentType(DocumentType.CNH)
                .file(DocumentFile.fromBytes(new byte[] {1, 2, 3}, "cnh-comprador.pdf"))
                .build();
    }

    private void failOnceThenSucceed(String scenario, com.github.tomakehurst.wiremock.client.MappingBuilder first, int failStatus,
                                     com.github.tomakehurst.wiremock.client.MappingBuilder second, com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder success) {
        server.stubFor(first.inScenario(scenario).whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(failStatus)).willSetStateTo("depois"));
        server.stubFor(second.inScenario(scenario).whenScenarioStateIs("depois").willReturn(success));
    }

    @Test
    void unifiedWithoutKeyIsNeverRetriedNorGivenAnAutomaticKey() {
        server.stubFor(post(urlEqualTo(path("v1/customer/unified"))).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.customers().upsert(unifiedRequest()))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(503));

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(path("v1/customer/unified"))));
        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).containsHeader("Idempotency-Key")).isFalse();
    }

    @Test
    void unifiedWithoutKeyIsNotRetriedOnNetworkFailureEither() {
        server.stubFor(post(urlEqualTo(path("v1/customer/unified"))).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client.customers().upsert(unifiedRequest())).isInstanceOf(CredicarroTimeoutException.class);

        server.verify(1, postRequestedFor(urlEqualTo(path("v1/customer/unified"))));
    }

    @Test
    void unifiedWithExplicitKeySendsItAndIsRetriedWithTheSameKey() {
        String url = path("v1/customer/unified");
        failOnceThenSucceed("unified", post(urlEqualTo(url)), 503, post(urlEqualTo(url)),
                jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}"));

        UUID id = client.customers().upsert(unifiedRequest(), "minha-chave-explicita");

        assertThat(id).isEqualTo(UUID.fromString(Fixtures.CUSTOMER_ID));
        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent).hasSize(2);
        assertThat(sent.get(0).getHeader("Idempotency-Key")).isEqualTo("minha-chave-explicita");
        assertThat(sent.get(1).getHeader("Idempotency-Key")).isEqualTo("minha-chave-explicita");
    }

    @Test
    void unifiedRejectsAnInvalidExplicitKeyBeforeSending() {
        assertThatThrownBy(() -> client.customers().upsert(unifiedRequest(), ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client.customers().upsert(unifiedRequest(), new String(new char[201]).replace('\0', 'a')))
                .isInstanceOf(IllegalArgumentException.class);

        server.verify(0, postRequestedFor(urlEqualTo(path("v1/customer/unified"))));
    }

    @Test
    void createCustomerGetsAnAutomaticKeyReusedAcrossRetries() {
        String url = path("v1/customer");
        failOnceThenSucceed("create", post(urlEqualTo(url)), 503, post(urlEqualTo(url)),
                jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}"));

        client.customers().create(createRequest());

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent).hasSize(2);
        String first = sent.get(0).getHeader("Idempotency-Key");
        assertThat(first).isNotBlank();
        assertThat(sent.get(1).getHeader("Idempotency-Key")).isEqualTo(first);
    }

    @Test
    void differentLogicalCallsGetDifferentAutomaticKeys() {
        String url = path("v1/customer");
        server.stubFor(post(urlEqualTo(url)).willReturn(jsonResponse(200, "{'id_cliente':'" + Fixtures.CUSTOMER_ID + "'}")));

        client.customers().create(createRequest());
        client.customers().create(createRequest());

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent.get(0).getHeader("Idempotency-Key")).isNotEqualTo(sent.get(1).getHeader("Idempotency-Key"));
    }

    @Test
    void addressAndProfessionalDataAlsoGetAnAutomaticKey() {
        server.stubFor(post(urlEqualTo(path("v1/customer/address"))).willReturn(aResponse().withStatus(202)));
        server.stubFor(post(urlEqualTo(path("v1/customer/professional"))).willReturn(aResponse().withStatus(202)));

        client.customers().upsertAddress("12345678909", CustomerAddress.builder().street("Rua A").neighborhood("B").city("C").state("SP").postalCode("01311000").build());
        client.customers().upsertProfessionalData("12345678909", ProfessionalData.builder().occupation(OccupationType.RETIRED).monthlyIncome(BigDecimal.TEN).build());

        server.verify(postRequestedFor(urlEqualTo(path("v1/customer/address"))).withHeader("Idempotency-Key", com.github.tomakehurst.wiremock.client.WireMock.matching(".+")));
        server.verify(postRequestedFor(urlEqualTo(path("v1/customer/professional"))).withHeader("Idempotency-Key", com.github.tomakehurst.wiremock.client.WireMock.matching(".+")));
    }

    @Test
    void documentUploadReusesTheSameKeyAcrossRetries() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        failOnceThenSucceed("upload", post(urlEqualTo(url)), 502, post(urlEqualTo(url)), aResponse().withStatus(204));

        client.proposals().uploadDocument(proposalId, upload());

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent).hasSize(2);
        String key = sent.get(0).getHeader("Idempotency-Key");
        assertThat(key).isNotBlank();
        assertThat(sent.get(1).getHeader("Idempotency-Key")).isEqualTo(key);
    }

    @Test
    void documentUploadUsesTheExplicitKeyWhenGiven() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(204)));

        client.proposals().uploadDocument(proposalId, upload(), "doc-7f3c2a10-comprador-cnh");

        server.verify(postRequestedFor(urlEqualTo(url)).withHeader("Idempotency-Key", equalTo("doc-7f3c2a10-comprador-cnh")));
    }

    @Test
    void documentUploadAlwaysSendsAKeyEvenWhenAutomaticKeysAreDisabled() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(204)));

        try (CredicarroClient noAutoKey = newClient(fastResilience().disableAutomaticIdempotencyKey(true).build())) {
            noAutoKey.proposals().uploadDocument(proposalId, upload());
        }

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent.get(0).getHeader("Idempotency-Key")).isNotBlank();
    }

    @Test
    void disablingAutomaticKeysRemovesTheHeaderAndTheRetry() {
        String url = path("v1/customer");
        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(503)));

        try (CredicarroClient noAutoKey = newClient(fastResilience().disableAutomaticIdempotencyKey(true).build())) {
            assertThatThrownBy(() -> noAutoKey.customers().create(createRequest())).isInstanceOf(CredicarroApiException.class);
        }

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).containsHeader("Idempotency-Key")).isFalse();
    }

    @Test
    void createWebhookIsNeverRetriedAndHasNoIdempotencyKey() {
        server.stubFor(post(urlEqualTo(path("v1/webhook"))).willReturn(aResponse().withStatus(503)));

        CreateWebhookRequest request = CreateWebhookRequest.builder().type(WebhookType.PROPOSAL)
                .url("https://parceiro.com.br/webhooks/credicarro").authenticationType(WebhookAuthenticationType.NONE).build();

        assertThatThrownBy(() -> client.webhooks().create(request)).isInstanceOf(CredicarroApiException.class);

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(path("v1/webhook"))));
        assertThat(sent).hasSize(1);
        assertThat(sent.get(0).containsHeader("Idempotency-Key")).isFalse();
    }

    @Test
    void vehicleDetailsHaveReplacementSemanticsAndAreRetried() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/vehicle-detail");
        failOnceThenSucceed("vehicle", post(urlEqualTo(url)), 503, post(urlEqualTo(url)), aResponse().withStatus(204));

        client.proposals().sendVehicleDetails(proposalId,
                VehicleDetails.builder().renavam("12345678900").chassis("9BWZZZ377VT004251").licensePlate("ABC1D23").color("PRATA").build());

        List<LoggedRequest> sent = server.findAll(postRequestedFor(urlEqualTo(url)));
        assertThat(sent).hasSize(2);
        assertThat(sent.get(0).containsHeader("Idempotency-Key")).isFalse();
    }

    @Test
    void putAndDeleteOfAWebhookAreRetried() {
        String url = path("v1/webhook/" + Fixtures.WEBHOOK_ID);
        failOnceThenSucceed("put", put(urlEqualTo(url)), 503, put(urlEqualTo(url)), aResponse().withStatus(204));
        failOnceThenSucceed("delete", delete(urlEqualTo(url)), 503, delete(urlEqualTo(url)), aResponse().withStatus(204));

        client.webhooks().update(webhookId, UpdateWebhookRequest.builder().type(WebhookType.PROPOSAL).url("https://parceiro.com.br/w").build());
        client.webhooks().delete(webhookId);

        server.verify(2, putRequestedFor(urlEqualTo(url)));
        server.verify(2, deleteRequestedFor(urlEqualTo(url)));
    }

    @Test
    void getIsRetriedOn500ByDefault() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        failOnceThenSucceed("get500", get(urlEqualTo(url)), 500, get(urlEqualTo(url)), jsonResponse(200, Fixtures.proposalV2()));

        assertThat(client.proposals().get(proposalId).getId()).isEqualTo(proposalId);

        server.verify(2, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void getIsNotRetriedOn500WhenRetryOn500IsDisabled() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(500)));

        try (CredicarroClient strict = newClient(fastResilience().retryOn500(false).build())) {
            assertThatThrownBy(() -> strict.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class);
        }

        server.verify(1, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void rateLimitHonoursRetryAfterAndIsRetried() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).inScenario("429").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0")).willSetStateTo("liberado"));
        server.stubFor(get(urlEqualTo(url)).inScenario("429").whenScenarioStateIs("liberado")
                .willReturn(jsonResponse(200, Fixtures.proposalV2())));

        assertThat(client.proposals().get(proposalId)).isNotNull();

        server.verify(2, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void networkFailureOnGetIsRetried() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).inScenario("rede").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)).willSetStateTo("ok"));
        server.stubFor(get(urlEqualTo(url)).inScenario("rede").whenScenarioStateIs("ok")
                .willReturn(jsonResponse(200, Fixtures.proposalV2())));

        assertThat(client.proposals().get(proposalId)).isNotNull();

        server.verify(2, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void otherClientErrorsAreNeverRetried() {
        for (int status : new int[] {400, 403, 404, 409, 422}) {
            server.resetRequests();
            String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
            server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)));

            assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class);

            server.verify(1, getRequestedFor(urlEqualTo(url)));
        }
    }

    @Test
    void retriesStopAtMaxRetryAttempts() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class);

        server.verify(3, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void maxRetryAttemptsOfOneMeansNoRetry() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(503)));

        CredicarroResilienceOptions once = fastResilience().maxRetryAttempts(1).build();
        try (CredicarroClient noRetry = newClient(once)) {
            assertThatThrownBy(() -> noRetry.proposals().get(proposalId)).isInstanceOf(CredicarroApiException.class);
        }

        server.verify(1, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void requestsWithoutAKeyNeverCarryTheHeaderOnGet() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID);
        server.stubFor(get(urlEqualTo(url)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        client.proposals().get(proposalId);

        server.verify(getRequestedFor(urlEqualTo(url)).withHeader("Idempotency-Key", absent()));
    }
}
