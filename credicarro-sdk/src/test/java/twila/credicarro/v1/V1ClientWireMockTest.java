package twila.credicarro.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.FinancingWithoutCpfOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingWithoutCpfOption;
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

    private static final String WITHOUT_CPF_OPTION = "{'valor_solicitado':40000,'prazo':24,'valor_parcela':2042.42,"
            + "'valor_tac':1500,'valor_boleto':3.5,'valor_seguro':0,'valor_iof':872,'percentual_iof':1.92,"
            + "'valor_credito_total':42722,'valor_divida_total':73527.12,'percentual_cet_mensal':3.62,"
            + "'percentual_cet_anual':53.25,'percentual_juros_mensal':3.29,'percentual_juros_anual':47.53,"
            + "'valor_retencao_diaria_aproximado':12.5,'valor_entrada_minima':5000}";

    @Test
    void createFinancingWithoutCpfMapsTheOptionsAndSendsNoIdempotencyKey() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf"))).willReturn(jsonResponse(200,
                "{'cotacoes':[" + WITHOUT_CPF_OPTION + "]}")));

        List<FinancingWithoutCpfOption> options = client.v1().simulations().createFinancingWithoutCpf(CreateFinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).downPayment(new BigDecimal("5000")).state("SP").build());

        assertThat(options).hasSize(1);
        FinancingWithoutCpfOption option = options.get(0);
        assertThat(option.getRequestedAmount()).isEqualByComparingTo("40000");
        assertThat(option.getTerm()).isEqualTo(24);
        assertThat(option.getInstallmentAmount()).isEqualByComparingTo("2042.42");
        assertThat(option.getTacAmount()).isEqualByComparingTo("1500");
        assertThat(option.getBoletoAmount()).isEqualByComparingTo("3.5");
        assertThat(option.getInsuranceAmount()).isEqualByComparingTo("0");
        assertThat(option.getIofAmount()).isEqualByComparingTo("872");
        assertThat(option.getIofRate()).isEqualByComparingTo("1.92");
        assertThat(option.getTotalCreditAmount()).isEqualByComparingTo("42722");
        assertThat(option.getTotalDebtAmount()).isEqualByComparingTo("73527.12");
        assertThat(option.getMonthlyCet()).isEqualByComparingTo("3.62");
        assertThat(option.getAnnualCet()).isEqualByComparingTo("53.25");
        assertThat(option.getMonthlyInterestRate()).isEqualByComparingTo("3.29");
        assertThat(option.getAnnualInterestRate()).isEqualByComparingTo("47.53");
        assertThat(option.getApproximateDailyRetention()).isEqualByComparingTo("12.5");
        assertThat(option.getMinimumDownPayment()).isEqualByComparingTo("5000");
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf")))
                .withRequestBody(equalToJson(json("{'placa':'ABC1D23','valorVeiculo':45000,'valorEntrada':5000,'uf':'SP'}")))
                .withoutHeader("Idempotency-Key"));
    }

    @Test
    void createFinancingWithoutCpfOmitsTheDownPaymentWhenNotInformed() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf"))).willReturn(jsonResponse(200,
                "{'cotacoes':[" + WITHOUT_CPF_OPTION.replace(",'valor_retencao_diaria_aproximado':12.5", "") + "]}")));

        List<FinancingWithoutCpfOption> options = client.v1().simulations().createFinancingWithoutCpf(CreateFinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).state("SP").build());

        assertThat(options.get(0).getApproximateDailyRetention()).isNull();
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf")))
                .withRequestBody(equalToJson(json("{'placa':'ABC1D23','valorVeiculo':45000,'uf':'SP'}"), false, false))
                .withRequestBody(com.github.tomakehurst.wiremock.client.WireMock.notMatching(".*valorEntrada.*"))
                .withoutHeader("Idempotency-Key"));
    }

    @Test
    void createFinancingWithoutCpfReturnsAnEmptyListWhenThereAreNoOptions() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf"))).willReturn(jsonResponse(200, "{'cotacoes':[]}")));

        assertThat(client.v1().simulations().createFinancingWithoutCpf(CreateFinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).state("SP").build())).isEmpty();
    }

    @Test
    void createFinancingWithoutCpfMapsNotFoundAndBusinessRuleErrors() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf"))).willReturn(jsonResponse(404,
                "{'tipo':'Fipe.VehiclePlateNotFound','titulo':'Veículo não encontrado','status':404}")));

        CreateFinancingWithoutCpfRequest request = CreateFinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).state("SP").build();

        assertThatThrownBy(() -> client.v1().simulations().createFinancingWithoutCpf(request))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> {
                    assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(404);
                    assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Fipe.VehiclePlateNotFound");
                });

        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/financing/without-cpf"))).willReturn(jsonResponse(422,
                "{'tipo':'Proposal.DownPaymentInvalid','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.v1().simulations().createFinancingWithoutCpf(request))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode()).isEqualTo("Proposal.DownPaymentInvalid"));
    }

    private static final String REFINANCING_WITHOUT_CPF_OPTION = "{'valor_solicitado':20000,'prazo':24,'valor_parcela':1100.5,"
            + "'valor_tac':1500,'valor_boleto':3.5,'valor_seguro':0,'valor_iof':400,'percentual_iof':1.92,"
            + "'valor_credito_total':21900,'valor_divida_total':26412,'percentual_cet_mensal':3.62,"
            + "'percentual_cet_anual':53.25,'percentual_juros_mensal':3.29,'percentual_juros_anual':47.53,"
            + "'valor_retencao_diaria_aproximado':12.5,'valor_minimo_refinanciavel':5000,'valor_maximo_refinanciavel':60000}";

    @Test
    void createRefinancingWithoutCpfMapsTheOptionsAndSendsNoIdempotencyKey() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf"))).willReturn(jsonResponse(200,
                "{'cotacoes':[" + REFINANCING_WITHOUT_CPF_OPTION + "]}")));

        List<RefinancingWithoutCpfOption> options = client.v1().simulations().createRefinancingWithoutCpf(CreateRefinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").requestedAmount(new BigDecimal("20000")).state("SP").build());

        assertThat(options).hasSize(1);
        RefinancingWithoutCpfOption option = options.get(0);
        assertThat(option.getRequestedAmount()).isEqualByComparingTo("20000");
        assertThat(option.getTerm()).isEqualTo(24);
        assertThat(option.getInstallmentAmount()).isEqualByComparingTo("1100.5");
        assertThat(option.getTacAmount()).isEqualByComparingTo("1500");
        assertThat(option.getBoletoAmount()).isEqualByComparingTo("3.5");
        assertThat(option.getInsuranceAmount()).isEqualByComparingTo("0");
        assertThat(option.getIofAmount()).isEqualByComparingTo("400");
        assertThat(option.getIofRate()).isEqualByComparingTo("1.92");
        assertThat(option.getTotalCreditAmount()).isEqualByComparingTo("21900");
        assertThat(option.getTotalDebtAmount()).isEqualByComparingTo("26412");
        assertThat(option.getMonthlyCet()).isEqualByComparingTo("3.62");
        assertThat(option.getAnnualCet()).isEqualByComparingTo("53.25");
        assertThat(option.getMonthlyInterestRate()).isEqualByComparingTo("3.29");
        assertThat(option.getAnnualInterestRate()).isEqualByComparingTo("47.53");
        assertThat(option.getApproximateDailyRetention()).isEqualByComparingTo("12.5");
        assertThat(option.getMinimumAmount()).isEqualByComparingTo("5000");
        assertThat(option.getMaximumAmount()).isEqualByComparingTo("60000");
        server.verify(postRequestedFor(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf")))
                .withRequestBody(equalToJson(json("{'placa':'ABC1D23','valorSolicitado':20000,'uf':'SP'}")))
                .withoutHeader("Idempotency-Key"));
    }

    @Test
    void createRefinancingWithoutCpfAcceptsAnOmittedDailyRetention() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf"))).willReturn(jsonResponse(200,
                "{'cotacoes':[" + REFINANCING_WITHOUT_CPF_OPTION.replace("'valor_retencao_diaria_aproximado':12.5,", "") + "]}")));

        List<RefinancingWithoutCpfOption> options = client.v1().simulations().createRefinancingWithoutCpf(CreateRefinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").requestedAmount(new BigDecimal("20000")).state("SP").build());

        assertThat(options.get(0).getApproximateDailyRetention()).isNull();
        assertThat(options.get(0).getMaximumAmount()).isEqualByComparingTo("60000");
    }

    @Test
    void createRefinancingWithoutCpfReturnsAnEmptyListWhenThereAreNoOptions() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf"))).willReturn(jsonResponse(200, "{'cotacoes':[]}")));

        assertThat(client.v1().simulations().createRefinancingWithoutCpf(CreateRefinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").requestedAmount(new BigDecimal("20000")).state("SP").build())).isEmpty();
    }

    @Test
    void createRefinancingWithoutCpfMapsNotFoundAndBusinessRuleErrors() {
        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf"))).willReturn(jsonResponse(404,
                "{'tipo':'Fipe.VehiclePlateNotFound','titulo':'Veículo não encontrado','status':404}")));

        CreateRefinancingWithoutCpfRequest request = CreateRefinancingWithoutCpfRequest.builder()
                .licensePlate("ABC1D23").requestedAmount(new BigDecimal("20000")).state("SP").build();

        assertThatThrownBy(() -> client.v1().simulations().createRefinancingWithoutCpf(request))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> {
                    assertThat(((CredicarroApiException) ex).getStatusCode()).isEqualTo(404);
                    assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Fipe.VehiclePlateNotFound");
                });

        server.stubFor(post(urlEqualTo(path("v1/proposal/simulation/refinancing/without-cpf"))).willReturn(jsonResponse(422,
                "{'tipo':'Proposal.RequestAmountInvalid','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.v1().simulations().createRefinancingWithoutCpf(request))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode()).isEqualTo("Proposal.RequestAmountInvalid"));
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
