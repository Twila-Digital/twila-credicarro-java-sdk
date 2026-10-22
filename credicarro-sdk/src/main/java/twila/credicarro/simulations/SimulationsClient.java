package twila.credicarro.simulations;

import java.util.List;
import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.CreateRefinancingSimulationRequest;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.Simulation;
import twila.credicarro.simulations.model.SimulationOption;

/**
 * Simulações (rotas v2, que selecionam a loja pelo CNPJ) e consultas de simulações v1. As rotas v1 sem CNPJ ficam em
 * {@code client.v1().simulations()}.
 */
public interface SimulationsClient {

    /**
     * Valor mínimo de entrada por prazo ({@code GET v2/proposal/simulation/range/financing}).
     */
    List<FinancingRangeOption> getFinancingRange(String licensePlate, String storeCnpj);

    /**
     * Faixa de valor refinanciável por prazo ({@code GET v2/proposal/simulation/range/refinancing}).
     */
    List<RefinancingRangeOption> getRefinancingRange(String licensePlate, String storeCnpj);

    /**
     * Cria a simulação de financiamento ({@code POST v2/proposal/simulation/financing}); uma opção por prazo.
     * O cliente precisa estar cadastrado.
     */
    List<SimulationOption> createFinancing(CreateFinancingSimulationRequest request);

    /**
     * Cria a simulação de refinanciamento ({@code POST v2/proposal/simulation/refinancing}); uma opção por prazo.
     */
    List<SimulationOption> createRefinancing(CreateRefinancingSimulationRequest request);

    /**
     * Consulta uma simulação pelo ID ({@code GET v1/proposal/simulation/:id}).
     */
    Simulation get(UUID simulationId);

    default PagedResult<Simulation> list() {
        return list(ListRequest.defaults());
    }

    /**
     * Lista simulações de forma paginada ({@code GET v1/proposal/simulation}).
     */
    PagedResult<Simulation> list(ListRequest request);
}
