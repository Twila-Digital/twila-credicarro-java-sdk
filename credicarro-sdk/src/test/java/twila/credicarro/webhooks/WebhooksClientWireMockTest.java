package twila.credicarro.webhooks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookStatus;
import twila.credicarro.webhooks.model.WebhookType;

class WebhooksClientWireMockTest extends ApiTestSupport {

    private final UUID webhookId = UUID.fromString(Fixtures.WEBHOOK_ID);

    @Test
    void createSendsNumericCodesAndReturnsTheSigningSecret() {
        server.stubFor(post(urlEqualTo(path("v1/webhook"))).willReturn(jsonResponse(201,
                "{'id_webhook':'" + Fixtures.WEBHOOK_ID + "','segredo_assinatura':'segredo-gerado-pela-api'}")));

        CreateWebhookResult result = client.webhooks().create(CreateWebhookRequest.builder()
                .type(WebhookType.PROPOSAL).url("https://parceiro.com.br/webhooks/credicarro")
                .authenticationType(WebhookAuthenticationType.BASIC).credential("dXN1YXJpbzpzZW5oYQ==").build());

        assertThat(result.getWebhookId()).isEqualTo(webhookId);
        assertThat(result.getSigningSecret()).isEqualTo("segredo-gerado-pela-api");
        assertThat(result.toString()).doesNotContain("segredo-gerado-pela-api");
        server.verify(postRequestedFor(urlEqualTo(path("v1/webhook"))).withRequestBody(equalToJson(json(
                "{'tipo':3,'url':'https://parceiro.com.br/webhooks/credicarro','tipo_autenticacao':2,'credencial':'dXN1YXJpbzpzZW5oYQ=='}"))));
    }

    @Test
    void createWithoutCredentialOmitsTheField() {
        server.stubFor(post(urlEqualTo(path("v1/webhook"))).willReturn(jsonResponse(201,
                "{'id_webhook':'" + Fixtures.WEBHOOK_ID + "','segredo_assinatura':'s'}")));

        client.webhooks().create(CreateWebhookRequest.builder().type(WebhookType.CUSTOMER).url("https://parceiro.com.br/w")
                .authenticationType(WebhookAuthenticationType.NONE).build());

        server.verify(postRequestedFor(urlEqualTo(path("v1/webhook"))).withRequestBody(equalToJson(json(
                "{'tipo':1,'url':'https://parceiro.com.br/w','tipo_autenticacao':1}"), false, false)));
    }

    @Test
    void requestToStringDoesNotExposeTheCredential() {
        CreateWebhookRequest request = CreateWebhookRequest.builder().type(WebhookType.PROPOSAL)
                .url("https://parceiro.com.br/w").authenticationType(WebhookAuthenticationType.JWT).credential("token-jwt-secreto").build();

        assertThat(request.toString()).doesNotContain("token-jwt-secreto");
    }

    @Test
    void invalidEndpointSurfacesTheBusinessRule() {
        server.stubFor(post(urlEqualTo(path("v1/webhook"))).willReturn(jsonResponse(422,
                "{'tipo':'Organization.Webhook.InvalidEndpoint','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.webhooks().create(CreateWebhookRequest.builder().type(WebhookType.PROPOSAL).url("https://localhost/x")
                .authenticationType(WebhookAuthenticationType.NONE).build()))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode()).isEqualTo("Organization.Webhook.InvalidEndpoint"));
    }

    @Test
    void listParsesEnumsFromTextAndPaginates() {
        String item = "{'id_webhook':'" + Fixtures.WEBHOOK_ID + "','tipo':'Proposal','url':'https://parceiro.com.br/webhooks/credicarro',"
                + "'tipo_autenticacao':'Basic','status':'Active'}";
        server.stubFor(get(urlEqualTo(path("v1/webhook?pagina_inicial=1&limite=10"))).willReturn(jsonResponse(200, Fixtures.page(item, false, false, 1, 10, 1))));

        PagedResult<Webhook> page = client.webhooks().list();

        assertThat(page.getItems()).hasSize(1);
        Webhook webhook = page.getItems().get(0);
        assertThat(webhook.getId()).isEqualTo(webhookId);
        assertThat(webhook.getType()).isEqualTo(WebhookType.PROPOSAL);
        assertThat(webhook.getUrl()).isEqualTo("https://parceiro.com.br/webhooks/credicarro");
        assertThat(webhook.getAuthenticationType()).isEqualTo(WebhookAuthenticationType.BASIC);
        assertThat(webhook.getStatus()).isEqualTo(WebhookStatus.ACTIVE);
        assertThat(page.getTotalCount()).isEqualTo(1L);
    }

    @Test
    void listFallsBackToUnknownForNewTexts() {
        String item = "{'id_webhook':'" + Fixtures.WEBHOOK_ID + "','tipo':'Novo','url':'u','tipo_autenticacao':'Oauth','status':'Suspended'}";
        server.stubFor(get(urlEqualTo(path("v1/webhook?pagina_inicial=2&limite=20"))).willReturn(jsonResponse(200, Fixtures.page(item, true, true, 2, 20, 41))));

        PagedResult<Webhook> page = client.webhooks().list(ListRequest.of(2, 20));

        Webhook webhook = page.getItems().get(0);
        assertThat(webhook.getType()).isEqualTo(WebhookType.UNKNOWN);
        assertThat(webhook.getAuthenticationType()).isEqualTo(WebhookAuthenticationType.UNKNOWN);
        assertThat(webhook.getStatus()).isEqualTo(WebhookStatus.UNKNOWN);
        assertThat(page.isHasNext()).isTrue();
    }

    @Test
    void updateSendsTypeUrlAndStatusCode() {
        server.stubFor(put(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).willReturn(aResponse().withStatus(204)));

        client.webhooks().update(webhookId, UpdateWebhookRequest.builder().type(WebhookType.PROPOSAL)
                .url("https://parceiro.com.br/webhooks/credicarro").status(WebhookStatus.INACTIVE).build());

        server.verify(putRequestedFor(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).withRequestBody(equalToJson(json(
                "{'tipo':3,'url':'https://parceiro.com.br/webhooks/credicarro','status':2}"))));
    }

    @Test
    void updateWithoutStatusOmitsItSoTheApiKeepsTheCurrentOne() {
        server.stubFor(put(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).willReturn(aResponse().withStatus(204)));

        client.webhooks().update(webhookId, UpdateWebhookRequest.builder().type(WebhookType.SIMULATION).url("https://parceiro.com.br/w").build());

        server.verify(putRequestedFor(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).withRequestBody(equalToJson(json(
                "{'tipo':2,'url':'https://parceiro.com.br/w'}"), false, false)));
    }

    @Test
    void deleteCallsTheWebhookRoute() {
        server.stubFor(delete(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).willReturn(aResponse().withStatus(204)));

        client.webhooks().delete(webhookId);

        server.verify(deleteRequestedFor(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))));
    }

    @Test
    void concurrencyConflictOnDeleteSurfacesAsAnApiException() {
        server.stubFor(delete(urlEqualTo(path("v1/webhook/" + Fixtures.WEBHOOK_ID))).willReturn(jsonResponse(409,
                "{'tipo':'Organization.Webhook.ConcurrencyConflict','titulo':'Conflito','status':409}")));

        assertThatThrownBy(() -> client.webhooks().delete(webhookId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Organization.Webhook.ConcurrencyConflict"));
    }
}
