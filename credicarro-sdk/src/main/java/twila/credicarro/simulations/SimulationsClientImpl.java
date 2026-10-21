package twila.credicarro.simulations;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.internal.generated.simulation.FinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.RefinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationListResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.QueryStringBuilder;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.SimulationMapper;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.CreateRefinancingSimulationRequest;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.Simulation;
import twila.credicarro.simulations.model.SimulationOption;

public final class SimulationsClientImpl implements SimulationsClient {

    private final ApiRequestExecutor executor;

    public SimulationsClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public List<FinancingRangeOption> getFinancingRange(String licensePlate, String storeCnpj) {
        ApiResponse response = executor.get(rangePath(Routes.V2_RANGE_FINANCING, licensePlate, storeCnpj));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, FinancingRangeResponseWire.class));
    }

    @Override
    public List<RefinancingRangeOption> getRefinancingRange(String licensePlate, String storeCnpj) {
        ApiResponse response = executor.get(rangePath(Routes.V2_RANGE_REFINANCING, licensePlate, storeCnpj));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, RefinancingRangeResponseWire.class));
    }

    @Override
    public List<SimulationOption> createFinancing(CreateFinancingSimulationRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V2_SIMULATION_FINANCING, JsonSupport.write(SimulationMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, SimulationListResponseWire.class));
    }

    @Override
    public List<SimulationOption> createRefinancing(CreateRefinancingSimulationRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V2_SIMULATION_REFINANCING, JsonSupport.write(SimulationMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, SimulationListResponseWire.class));
    }

    @Override
    public Simulation get(UUID simulationId) {
        Objects.requireNonNull(simulationId, "simulationId é obrigatório.");

        ApiResponse response = executor.get(Routes.format(Routes.V1_SIMULATION_BY_ID, simulationId));
        ApiRequestExecutor.ensureSuccess(response);

        return SimulationMapper.toPublic(JsonSupport.read(response, SimulationWire.class));
    }

    @Override
    public PagedResult<Simulation> list(ListRequest request) {
        ApiResponse response = executor.get(JsonSupport.pagedPath(Routes.V1_SIMULATIONS, request));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.readPaged(response, SimulationWire.class, SimulationMapper::toPublic);
    }

    private static String rangePath(String route, String licensePlate, String storeCnpj) {
        return new QueryStringBuilder()
                .add("placa", licensePlate)
                .add("cnpjLoja", storeCnpj)
                .build(route);
    }
}
