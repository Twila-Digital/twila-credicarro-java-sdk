package twila.credicarro.v1;

import java.util.List;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.SimulationOption;

/**
 * Simulações v1 (sem {@code cnpjLoja}).
 */
public interface V1SimulationsClient {

    /**
     * {@code GET v1/proposal/simulation/range/financing}.
     */
    List<FinancingRangeOption> getFinancingRange(String licensePlate);

    /**
     * {@code GET v1/proposal/simulation/range/refinancing}.
     */
    List<RefinancingRangeOption> getRefinancingRange(String licensePlate);

    /**
     * {@code POST v1/proposal/simulation/financing}.
     */
    List<SimulationOption> createFinancing(V1CreateFinancingSimulationRequest request);

    /**
     * {@code POST v1/proposal/simulation/refinancing}.
     */
    List<SimulationOption> createRefinancing(V1CreateRefinancingSimulationRequest request);
}
