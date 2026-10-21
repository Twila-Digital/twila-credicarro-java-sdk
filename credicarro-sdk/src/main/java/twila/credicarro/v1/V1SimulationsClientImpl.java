package twila.credicarro.v1;

import java.util.List;
import java.util.Objects;
import twila.credicarro.internal.generated.simulation.FinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.RefinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationListResponseWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.QueryStringBuilder;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.SimulationMapper;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.SimulationOption;

public final class V1SimulationsClientImpl implements V1SimulationsClient {

    private final ApiRequestExecutor executor;

    public V1SimulationsClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public List<FinancingRangeOption> getFinancingRange(String licensePlate) {
        ApiResponse response = executor.get(new QueryStringBuilder().add("placa", licensePlate).build(Routes.V1_RANGE_FINANCING));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, FinancingRangeResponseWire.class));
    }

    @Override
    public List<RefinancingRangeOption> getRefinancingRange(String licensePlate) {
        ApiResponse response = executor.get(new QueryStringBuilder().add("placa", licensePlate).build(Routes.V1_RANGE_REFINANCING));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, RefinancingRangeResponseWire.class));
    }

    @Override
    public List<SimulationOption> createFinancing(V1CreateFinancingSimulationRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V1_SIMULATION_FINANCING, JsonSupport.write(SimulationMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, SimulationListResponseWire.class));
    }

    @Override
    public List<SimulationOption> createRefinancing(V1CreateRefinancingSimulationRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V1_SIMULATION_REFINANCING, JsonSupport.write(SimulationMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, SimulationListResponseWire.class));
    }
}
