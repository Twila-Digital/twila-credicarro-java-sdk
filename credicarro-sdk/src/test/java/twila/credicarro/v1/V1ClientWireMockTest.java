package twila.credicarro.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.SimulationOption;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

class V1ClientWireMockTest extends ApiTestSupport {

    @Test
    void financingRangeUsesOnlyThePlate() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation/range/financing?placa=ABC-1234")))
                .willReturn(jsonResponse(200, "{'opcoes_por_prazo':[{'prazo':12,'valor_minimo_entrada':5000}]}")));

        List<FinancingRangeOption> options = client.v1().simulations().getFinancingRange("ABC-1234");

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getTerm()).isEqualTo(12);
        assertThat(options.get(0).getMinimumDownPayment()).isEqualByComparingTo("5000");
    }

    @Test
    void refinancingRangeUsesOnlyThePlate() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation/range/refinancing?placa=ABC1D23")))
                .willReturn(jsonResponse(200, "{'opcoes_por_prazo':[{'prazo':6,'valor_minimo_refinanciavel':12480,'valor_maximo_refinanciavel':35720}]}")));

        List<RefinancingRangeOption> options = client.v1().simulations().getRefinancingRange("ABC1D23");

        assertThat(options.get(0).getMaximumAmount()).isEqualByComparingTo("35720");
    }

    @Test
    void createFinancingHasNoStoreCnpjAndGetsAnAutomaticKey() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing"))).willReturn(jsonResponse(201,
                "{'simulacoes':[" + Fixtures.simulationOption(Fixtures.SIMULATION_ID, 24) + "]}")));

        List<SimulationOption> options = client.v1().simulations().createFinancing(V1CreateFinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).downPayment(new BigDecimal("5000"))
                .state("SP").isZeroKm(true).build());

        assertThat(options).hasSize(1);
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal/simulation/financing")))
                .withRequestBody(equalToJson(json("{'cpf':'12345678909','placa':'ABC1D23','valorVeiculo':45000,'valorEntrada':5000,'uf':'SP','isVeiculoZeroKm':true}")))
                .withHeader("Idempotency-Key", com.github.tomakehurst.wiremock.client.WireMock.matching(".+")));
    }

    @Test
    void createRefinancingHasNoStoreCnpj() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing"))).willReturn(jsonResponse(201,
                "{'simulacoes':[" + Fixtures.simulationOption(Fixtures.SIMULATION_ID, 12) + "]}")));

        List<SimulationOption> options = client.v1().simulations().createRefinancing(V1CreateRefinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").requestedAmount(new BigDecimal("25000")).build());

        assertThat(options.get(0).getTerm()).isEqualTo(12);
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal/simulation/refinancing")))
                .withRequestBody(equalToJson(json("{'cpf':'12345678909','placa':'ABC1D23','valorSolicitado':25000}"))));
    }

    @Test
    void createProposalPostsToTheV1Route() {
        server.stubFor(post(urlEqualTo(path("v1/proposal"))).willReturn(jsonResponse(201, "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "'}")));

        UUID id = client.v1().proposals().create(CreateProposalRequest.builder().cpf("12345678909").simulationId(UUID.fromString(Fixtures.SIMULATION_ID)).build());

        assertThat(id).isEqualTo(UUID.fromString(Fixtures.PROPOSAL_ID));
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal")))
                .withRequestBody(equalToJson(json("{'cpf':'12345678909','simulacaoId':'" + Fixtures.SIMULATION_ID + "'}")))
                .withHeader("Idempotency-Key", com.github.tomakehurst.wiremock.client.WireMock.matching(".+")));
    }

    @Test
    void getProposalReturnsTheV1Summary() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/" + Fixtures.PROPOSAL_ID))).willReturn(jsonResponse(200,
                "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "','numero':1,'id_simulacao':'" + Fixtures.SIMULATION_ID
                        + "','tipo':'Refinancing','status':'Completed','comprador':{'cpf':'123.456.789-01'},'data_criacao':'2025-10-17T19:05:08.984Z'}")));

        ProposalSummary summary = client.v1().proposals().get(UUID.fromString(Fixtures.PROPOSAL_ID));

        assertThat(summary.getType()).isEqualTo(ProposalType.REFINANCING);
        assertThat(summary.getStatus()).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(summary.getBuyerCpf()).isEqualTo("123.456.789-01");
        assertThat(summary.getSellerCpf()).isNull();
    }
}
