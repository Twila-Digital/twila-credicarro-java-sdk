package twila.credicarro.webhooks;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.simulations.model.CommissionReturn;
import twila.credicarro.simulations.model.CommissionReturnTarget;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.CreateRefinancingSimulationRequest;
import twila.credicarro.simulations.model.SimulationType;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;
import twila.credicarro.webhooks.model.WebhookDelivery;
import twila.credicarro.webhooks.model.WebhookDeliveryChartPoint;
import twila.credicarro.webhooks.model.WebhookDeliveryFilter;
import twila.credicarro.webhooks.model.WebhookResendResult;
import twila.credicarro.webhooks.model.WebhookType;

class WebhookDeliveriesWireMockTest extends ApiTestSupport {

    private static final String AUDIT_ID = "7c9e6679-7425-40de-944b-e07fc1f90ae7";

    @Test
    void listDeliveriesSendsFiltersAndPagingAndMapsTheItems() {
        server.stubFor(get(urlPathEqualTo(path("v1/webhook/audit"))).willReturn(jsonResponse(200,
                "{'itens':["
                        + "{'id_auditoria':'" + AUDIT_ID + "','id_webhook':'" + Fixtures.WEBHOOK_ID + "','tipo':'Proposal',"
                        + "'requisicao':'corpo-sensivel','resposta':'ok','status_http':200,'data_criacao':'2026-11-05T14:32:10Z'},"
                        + "{'id_auditoria':'" + AUDIT_ID + "','id_webhook':null,'tipo':'Algo','requisicao':'x','resposta':'',"
                        + "'status_http':null,'data_criacao':'2026-11-05T14:33:00Z'}],"
                        + "'pagina':{'tem_proximo':true,'tem_anterior':false,'numero':2,'tamanho':50,'total':51}}")));

        PagedResult<WebhookDelivery> page = client.webhooks().listDeliveries(
                WebhookDeliveryFilter.builder()
                        .initialDate(OffsetDateTime.of(2026, 11, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-3)))
                        .proposalNumber("12345")
                        .statusCode(500)
                        .build(),
                ListRequest.of(2, 50));

        server.verify(getRequestedFor(urlPathEqualTo(path("v1/webhook/audit")))
                .withQueryParam("data_inicial", equalTo("2026-11-01T00:00:00-03:00"))
                .withQueryParam("numero_proposta", equalTo("12345"))
                .withQueryParam("status_http", equalTo("500"))
                .withQueryParam("pagina_inicial", equalTo("2"))
                .withQueryParam("limite", equalTo("50"))
                .withQueryParam("data_final", absent()));

        WebhookDelivery first = page.getItems().get(0);
        assertThat(first.getId()).isEqualTo(UUID.fromString(AUDIT_ID));
        assertThat(first.getWebhookId()).isEqualTo(UUID.fromString(Fixtures.WEBHOOK_ID));
        assertThat(first.getType()).isEqualTo(WebhookType.PROPOSAL);
        assertThat(first.getStatusCode()).isEqualTo(200);
        assertThat(first.toString()).doesNotContain("corpo-sensivel");

        WebhookDelivery second = page.getItems().get(1);
        assertThat(second.getWebhookId()).isNull();
        assertThat(second.getType()).isEqualTo(WebhookType.UNKNOWN);
        assertThat(second.getStatusCode()).isNull();
    }

    @Test
    void listDeliveriesWithoutFiltersOnlySendsPaging() {
        server.stubFor(get(urlEqualTo(path("v1/webhook/audit?pagina_inicial=1&limite=10")))
                .willReturn(jsonResponse(200, "{'itens':[],'pagina':{'numero':1,'tamanho':10,'total':0}}")));

        client.webhooks().listDeliveries();

        server.verify(getRequestedFor(urlEqualTo(path("v1/webhook/audit?pagina_inicial=1&limite=10"))));
    }

    @Test
    void getDeliveryChartSendsThePeriodAndMapsThePoints() {
        server.stubFor(get(urlPathEqualTo(path("v1/webhook/audit/chart"))).willReturn(jsonResponse(200,
                "[{'rotulo':'04','sucessos':42,'erros':0},{'rotulo':'05','sucessos':38,'erros':3}]")));

        List<WebhookDeliveryChartPoint> points = client.webhooks().getDeliveryChart(WebhookDeliveryFilter.builder()
                .initialDate(OffsetDateTime.of(2026, 11, 4, 0, 0, 0, 0, ZoneOffset.UTC))
                .finalDate(OffsetDateTime.of(2026, 11, 5, 23, 59, 59, 0, ZoneOffset.UTC))
                .build());

        server.verify(getRequestedFor(urlPathEqualTo(path("v1/webhook/audit/chart")))
                .withQueryParam("data_inicial", equalTo("2026-11-04T00:00:00Z"))
                .withQueryParam("data_final", equalTo("2026-11-05T23:59:59Z")));
        assertThat(points).containsExactly(new WebhookDeliveryChartPoint("04", 42, 0), new WebhookDeliveryChartPoint("05", 38, 3));
    }

    @Test
    void getDeliveryChartRequiresThePeriod() {
        assertThatThrownBy(() -> client.webhooks().getDeliveryChart(WebhookDeliveryFilter.builder()
                .initialDate(OffsetDateTime.now()).build()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void resendPostsWithoutIdempotencyKeyAndReturnsTheResult() {
        server.stubFor(post(urlEqualTo(path("v1/webhook/audit/" + AUDIT_ID + "/resend")))
                .willReturn(jsonResponse(200, "{'status_http':null,'resposta':''}")));

        WebhookResendResult result = client.webhooks().resend(UUID.fromString(AUDIT_ID));

        server.verify(postRequestedFor(urlEqualTo(path("v1/webhook/audit/" + AUDIT_ID + "/resend")))
                .withHeader("Idempotency-Key", absent()));
        assertThat(result.getStatusCode()).isNull();
        assertThat(result.getResponse()).isEmpty();
    }

    @Test
    void resendIsNotRetriedOn503() {
        server.stubFor(post(urlEqualTo(path("v1/webhook/audit/" + AUDIT_ID + "/resend"))).willReturn(jsonResponse(503,
                "{'tipo':'Organization.Webhook.Resend.DependencyUnavailable','titulo':'Indisponível','status':503}")));

        assertThatThrownBy(() -> client.webhooks().resend(UUID.fromString(AUDIT_ID)))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getErrorCode())
                        .isEqualTo("Organization.Webhook.Resend.DependencyUnavailable"));

        server.verify(1, postRequestedFor(urlEqualTo(path("v1/webhook/audit/" + AUDIT_ID + "/resend"))));
    }

    @Test
    void getCommissionReturnsSendsTheStoreAndMapsTheRanges() {
        server.stubFor(get(urlPathEqualTo(path("v2/proposal/simulation/commission-returns"))).willReturn(jsonResponse(200,
                "{'retornos_comissao':["
                        + "{'tipo':'Financing','alvo':'Store','retorno_minimo':0,'retorno_maximo':4,'permite_taxa_padrao':true},"
                        + "{'tipo':'Outro','alvo':'Novo','retorno_minimo':1,'retorno_maximo':3,'permite_taxa_padrao':false}]}")));

        List<CommissionReturn> returns = client.simulations().getCommissionReturns("11.222.333/0001-81");

        server.verify(getRequestedFor(urlPathEqualTo(path("v2/proposal/simulation/commission-returns")))
                .withQueryParam("cnpjLoja", equalTo("11.222.333/0001-81")));
        assertThat(returns).containsExactly(
                new CommissionReturn(SimulationType.FINANCING, CommissionReturnTarget.STORE, 0, 4, true),
                new CommissionReturn(SimulationType.UNKNOWN, CommissionReturnTarget.UNKNOWN, 1, 3, false));
    }

    @Test
    void simulationsSendCommissionReturnOnlyWhenProvided() {
        String options = "{'simulacoes':[" + Fixtures.simulationOption(Fixtures.SIMULATION_ID, 36) + "]}";
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/financing"))).willReturn(jsonResponse(201, options)));
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/refinancing"))).willReturn(jsonResponse(201, options)));

        client.simulations().createFinancing(CreateFinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).downPayment(new BigDecimal("5000"))
                .state("SP").isZeroKm(false).storeCnpj("11222333000181").commissionReturn(2).build());
        client.simulations().createRefinancing(CreateRefinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").requestedAmount(new BigDecimal("25000")).storeCnpj("11222333000181").build());

        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/simulation/financing")))
                .withRequestBody(matchingJsonPath("$.retornoComissao", equalTo("2"))));
        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/simulation/refinancing")))
                .withRequestBody(matchingJsonPath("$.retornoComissao", absent())));
    }

    @Test
    void commissionReturnOutOfRangeIsABusinessRule() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/financing"))).willReturn(jsonResponse(422,
                "{'tipo':'Store.CommissionReturn.OutOfRange','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.simulations().createFinancing(CreateFinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).downPayment(new BigDecimal("5000"))
                .state("SP").isZeroKm(false).storeCnpj("11222333000181").commissionReturn(9).build()))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode())
                        .isEqualTo("Store.CommissionReturn.OutOfRange"));
    }
}
