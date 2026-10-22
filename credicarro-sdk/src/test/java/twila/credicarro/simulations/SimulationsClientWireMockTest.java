package twila.credicarro.simulations;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.CreateRefinancingSimulationRequest;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.Simulation;
import twila.credicarro.simulations.model.SimulationOption;
import twila.credicarro.simulations.model.SimulationType;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

class SimulationsClientWireMockTest extends ApiTestSupport {

    private static final String SIMULATION_V1 = "{'id_simulacao':'" + Fixtures.SIMULATION_ID + "','prazo':24,'valor_parcela':2356,"
            + "'valor_solicitado':37908,'valor_entrada':9786,'valor_bem':47694,'valor_tac':1500,'taxa_juros_negociada':2.79,"
            + "'valor_registro':487,'uf':'SP','tipo':'Financing',"
            + "'transacao':{'valor_total_credito':78495,'valor_total_debito':78500},"
            + "'juros':{'percentual_juros_mensal':3.29,'percentual_juros_anual':39.48},"
            + "'cet':{'percentual_mensal':3.62,'percentual_anual':43.44},"
            + "'iof':{'valor':872,'percentual':1.92},"
            + "'veiculo':{'marca':'Honda','modelo':'Civic','versao':'Turing','ano':2024,'valor':124654,'placa':'MWO-3910',"
            + "'veiculo_zero_km':false,'chassi':'1Y5Ye4U8kydsr4828','renavam':'70502561697','cor':'Prata'},"
            + "'cliente':{'cpf':'123.456.789-01'},'data_criacao':'2025-10-17T19:05:08.984Z'}";

    @Test
    void financingRangeSendsPlateAndStoreCnpjAndMapsTheOptions() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/simulation/range/financing?placa=ABC1D23&cnpjLoja=11.222.333%2F0001-81")))
                .willReturn(jsonResponse(200, "{'opcoes_por_prazo':[{'prazo':12,'valor_minimo_entrada':5000.00},{'prazo':24,'valor_minimo_entrada':7000.5}]}")));

        List<FinancingRangeOption> options = client.simulations().getFinancingRange("ABC1D23", "11.222.333/0001-81");

        assertThat(options).hasSize(2);
        assertThat(options.get(0).getTerm()).isEqualTo(12);
        assertThat(options.get(0).getMinimumDownPayment()).isEqualByComparingTo("5000.00");
        assertThat(options.get(1).getMinimumDownPayment()).isEqualByComparingTo("7000.5");
    }

    @Test
    void refinancingRangeMapsMinimumAndMaximum() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/simulation/range/refinancing?placa=ABC1D23&cnpjLoja=11222333000181")))
                .willReturn(jsonResponse(200, "{'opcoes_por_prazo':[{'prazo':6,'valor_minimo_refinanciavel':12480,'valor_maximo_refinanciavel':35720}]}")));

        List<RefinancingRangeOption> options = client.simulations().getRefinancingRange("ABC1D23", "11222333000181");

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getTerm()).isEqualTo(6);
        assertThat(options.get(0).getMinimumAmount()).isEqualByComparingTo("12480");
        assertThat(options.get(0).getMaximumAmount()).isEqualByComparingTo("35720");
    }

    @Test
    void createFinancingSendsTheV2PayloadAndMapsEveryOptionField() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/financing"))).willReturn(jsonResponse(201,
                "{'simulacoes':[" + Fixtures.simulationOption(Fixtures.SIMULATION_ID, 36) + "]}")));

        List<SimulationOption> options = client.simulations().createFinancing(CreateFinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").vehicleAmount(new BigDecimal("45000")).downPayment(new BigDecimal("5000"))
                .state("SP").isZeroKm(false).storeCnpj("11.222.333/0001-81").build());

        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/simulation/financing"))).withRequestBody(equalToJson(json(
                "{'cpf':'12345678909','placa':'ABC1D23','valorVeiculo':45000,'valorEntrada':5000,'uf':'SP','isVeiculoZeroKm':false,'cnpjLoja':'11.222.333/0001-81'}"))));

        assertThat(options).hasSize(1);
        SimulationOption option = options.get(0);
        assertThat(option.getSimulationId()).isEqualTo(UUID.fromString(Fixtures.SIMULATION_ID));
        assertThat(option.getRequestedAmount()).isEqualByComparingTo("40000");
        assertThat(option.getTerm()).isEqualTo(36);
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
    }

    @Test
    void optionalDailyRetentionIsNullWhenOmitted() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/refinancing"))).willReturn(jsonResponse(201,
                "{'simulacoes':[{'id_simulacao':'" + Fixtures.SIMULATION_ID + "','prazo':6,'valor_parcela':100}]}")));

        List<SimulationOption> options = client.simulations().createRefinancing(CreateRefinancingSimulationRequest.builder()
                .cpf("12345678909").licensePlate("ABC1D23").requestedAmount(new BigDecimal("25000")).storeCnpj("11222333000181").build());

        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/simulation/refinancing"))).withRequestBody(equalToJson(json(
                "{'cpf':'12345678909','placa':'ABC1D23','valorSolicitado':25000,'cnpjLoja':'11222333000181'}"))));
        assertThat(options.get(0).getApproximateDailyRetention()).isNull();
    }

    @Test
    void intermediationStoreSurfacesAsABusinessRuleError() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/simulation/financing"))).willReturn(jsonResponse(422,
                "{'tipo':'Store.Integration.Intermediation','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.simulations().createFinancing(CreateFinancingSimulationRequest.builder().cpf("12345678909").build()))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode()).isEqualTo("Store.Integration.Intermediation"));
    }

    @Test
    void getMapsTheFullSimulationIncludingNestedObjects() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation/" + Fixtures.SIMULATION_ID))).willReturn(jsonResponse(200, SIMULATION_V1)));

        Simulation simulation = client.simulations().get(UUID.fromString(Fixtures.SIMULATION_ID));

        assertThat(simulation.getSimulationId()).isEqualTo(UUID.fromString(Fixtures.SIMULATION_ID));
        assertThat(simulation.getTerm()).isEqualTo(24L);
        assertThat(simulation.getInstallmentAmount()).isEqualByComparingTo("2356");
        assertThat(simulation.getRequestedAmount()).isEqualByComparingTo("37908");
        assertThat(simulation.getDownPayment()).isEqualByComparingTo("9786");
        assertThat(simulation.getAssetAmount()).isEqualByComparingTo("47694");
        assertThat(simulation.getTacAmount()).isEqualByComparingTo("1500");
        assertThat(simulation.getNegotiatedInterestRate()).isEqualByComparingTo("2.79");
        assertThat(simulation.getRegistrationAmount()).isEqualByComparingTo("487");
        assertThat(simulation.getState()).isEqualTo("SP");
        assertThat(simulation.getType()).isEqualTo(SimulationType.FINANCING);
        assertThat(simulation.getTransaction().getTotalCredit()).isEqualByComparingTo("78495");
        assertThat(simulation.getTransaction().getTotalDebit()).isEqualByComparingTo("78500");
        assertThat(simulation.getInterest().getMonthlyRate()).isEqualByComparingTo("3.29");
        assertThat(simulation.getInterest().getAnnualRate()).isEqualByComparingTo("39.48");
        assertThat(simulation.getCet().getMonthlyRate()).isEqualByComparingTo("3.62");
        assertThat(simulation.getCet().getAnnualRate()).isEqualByComparingTo("43.44");
        assertThat(simulation.getIof().getAmount()).isEqualByComparingTo("872");
        assertThat(simulation.getIof().getRate()).isEqualByComparingTo("1.92");
        assertThat(simulation.getVehicle().getBrand()).isEqualTo("Honda");
        assertThat(simulation.getVehicle().getYear()).isEqualTo(2024);
        assertThat(simulation.getVehicle().getValue()).isEqualByComparingTo("124654");
        assertThat(simulation.getVehicle().getZeroKm()).isFalse();
        assertThat(simulation.getVehicle().getChassis()).isEqualTo("1Y5Ye4U8kydsr4828");
        assertThat(simulation.getVehicle().getColor()).isEqualTo("Prata");
        assertThat(simulation.getCustomerCpf()).isEqualTo("123.456.789-01");
        assertThat(simulation.getCreatedAt()).isNotNull();
    }

    @Test
    void unknownSimulationTypeFallsBackToUnknown() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation/" + Fixtures.SIMULATION_ID)))
                .willReturn(jsonResponse(200, "{'id_simulacao':'" + Fixtures.SIMULATION_ID + "','tipo':'NovoTipo'}")));

        Simulation simulation = client.simulations().get(UUID.fromString(Fixtures.SIMULATION_ID));

        assertThat(simulation.getType()).isEqualTo(SimulationType.UNKNOWN);
        assertThat(simulation.getVehicle()).isNull();
        assertThat(simulation.getCustomerCpf()).isNull();
    }

    @Test
    void listUsesPaginationParametersAndMapsThePage() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation?pagina_inicial=2&limite=15")))
                .willReturn(jsonResponse(200, Fixtures.page(SIMULATION_V1, true, true, 2, 15, 45))));

        PagedResult<Simulation> page = client.simulations().list(ListRequest.of(2, 15));

        assertThat(page.getItems()).hasSize(1);
        assertThat(page.isHasNext()).isTrue();
        assertThat(page.isHasPrevious()).isTrue();
        assertThat(page.getPage()).isEqualTo(2);
        assertThat(page.getPageSize()).isEqualTo(15);
        assertThat(page.getTotalCount()).isEqualTo(45L);
    }

    @Test
    void listWithoutArgumentsUsesPageOneAndTenItems() {
        server.stubFor(get(urlEqualTo(path("v1/proposal/simulation?pagina_inicial=1&limite=10")))
                .willReturn(jsonResponse(200, Fixtures.page("", false, false, 1, 10, 0))));

        PagedResult<Simulation> page = client.simulations().list();

        assertThat(page.getItems()).isEmpty();
        server.verify(getRequestedFor(urlEqualTo(path("v1/proposal/simulation?pagina_inicial=1&limite=10"))));
    }

    @Test
    void listRequestValidatesTheRange() {
        assertThatThrownBy(() -> ListRequest.of(0, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ListRequest.of(1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ListRequest.of(1, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThat(ListRequest.of(1, 100).getPageSize()).isEqualTo(100);
        assertThat(ListRequest.builder().build().getPage()).isEqualTo(1);
        assertThat(ListRequest.builder().build().getPageSize()).isEqualTo(10);
    }
}
