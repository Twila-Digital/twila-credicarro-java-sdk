package twila.credicarro.internal.mapping;

import java.util.ArrayList;
import java.util.List;
import twila.credicarro.internal.generated.simulation.CetWire;
import twila.credicarro.internal.generated.simulation.CommissionReturnWire;
import twila.credicarro.internal.generated.simulation.CreateFinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.CreateRefinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.FinancingRangeOptionWire;
import twila.credicarro.internal.generated.simulation.FinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.InterestWire;
import twila.credicarro.internal.generated.simulation.IofWire;
import twila.credicarro.internal.generated.simulation.ListCommissionReturnsResponseWire;
import twila.credicarro.internal.generated.simulation.RefinancingRangeOptionWire;
import twila.credicarro.internal.generated.simulation.RefinancingRangeResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationListResponseWire;
import twila.credicarro.internal.generated.simulation.SimulationOptionWire;
import twila.credicarro.internal.generated.simulation.SimulationWire;
import twila.credicarro.internal.generated.simulation.TransactionWire;
import twila.credicarro.internal.generated.simulation.V1CreateFinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.V1CreateRefinancingSimulationRequestWire;
import twila.credicarro.internal.generated.simulation.VehicleWire;
import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.simulations.model.CommissionReturn;
import twila.credicarro.simulations.model.CommissionReturnTarget;
import twila.credicarro.simulations.model.CommissionReturnType;
import twila.credicarro.simulations.model.CreateFinancingSimulationRequest;
import twila.credicarro.simulations.model.CreateRefinancingSimulationRequest;
import twila.credicarro.simulations.model.FinancingRangeOption;
import twila.credicarro.simulations.model.RefinancingRangeOption;
import twila.credicarro.simulations.model.Simulation;
import twila.credicarro.simulations.model.SimulationIof;
import twila.credicarro.simulations.model.SimulationOption;
import twila.credicarro.simulations.model.SimulationRate;
import twila.credicarro.simulations.model.SimulationTransaction;
import twila.credicarro.simulations.model.SimulationType;
import twila.credicarro.simulations.model.SimulationVehicle;
import twila.credicarro.v1.V1CreateFinancingSimulationRequest;
import twila.credicarro.v1.V1CreateRefinancingSimulationRequest;

public final class SimulationMapper {

    private SimulationMapper() {
    }

    public static CreateFinancingSimulationRequestWire toWire(CreateFinancingSimulationRequest request) {
        CreateFinancingSimulationRequestWire wire = new CreateFinancingSimulationRequestWire();
        wire.cpf = request.getCpf();
        wire.placa = request.getLicensePlate();
        wire.valorVeiculo = request.getVehicleAmount();
        wire.valorEntrada = request.getDownPayment();
        wire.uf = request.getState();
        wire.isVeiculoZeroKm = request.isZeroKm();
        wire.retornoComissao = request.getCommissionReturn();
        wire.cnpjLoja = request.getStoreCnpj();
        return wire;
    }

    public static CreateRefinancingSimulationRequestWire toWire(CreateRefinancingSimulationRequest request) {
        CreateRefinancingSimulationRequestWire wire = new CreateRefinancingSimulationRequestWire();
        wire.cpf = request.getCpf();
        wire.placa = request.getLicensePlate();
        wire.valorSolicitado = request.getRequestedAmount();
        wire.retornoComissao = request.getCommissionReturn();
        wire.cnpjLoja = request.getStoreCnpj();
        return wire;
    }

    public static V1CreateFinancingSimulationRequestWire toWire(V1CreateFinancingSimulationRequest request) {
        V1CreateFinancingSimulationRequestWire wire = new V1CreateFinancingSimulationRequestWire();
        wire.cpf = request.getCpf();
        wire.placa = request.getLicensePlate();
        wire.valorVeiculo = request.getVehicleAmount();
        wire.valorEntrada = request.getDownPayment();
        wire.uf = request.getState();
        wire.isVeiculoZeroKm = request.isZeroKm();
        return wire;
    }

    public static V1CreateRefinancingSimulationRequestWire toWire(V1CreateRefinancingSimulationRequest request) {
        V1CreateRefinancingSimulationRequestWire wire = new V1CreateRefinancingSimulationRequestWire();
        wire.cpf = request.getCpf();
        wire.placa = request.getLicensePlate();
        wire.valorSolicitado = request.getRequestedAmount();
        return wire;
    }

    public static List<FinancingRangeOption> toPublic(FinancingRangeResponseWire wire) {
        List<FinancingRangeOption> options = new ArrayList<>();

        if (wire != null && wire.opcoesPorPrazo != null) {
            for (FinancingRangeOptionWire option : wire.opcoesPorPrazo)
                options.add(new FinancingRangeOption(option.prazo, option.valorMinimoEntrada));
        }

        return options;
    }

    public static List<RefinancingRangeOption> toPublic(RefinancingRangeResponseWire wire) {
        List<RefinancingRangeOption> options = new ArrayList<>();

        if (wire != null && wire.opcoesPorPrazo != null) {
            for (RefinancingRangeOptionWire option : wire.opcoesPorPrazo)
                options.add(new RefinancingRangeOption(option.prazo, option.valorMinimoRefinanciavel, option.valorMaximoRefinanciavel));
        }

        return options;
    }

    public static List<CommissionReturn> toPublic(ListCommissionReturnsResponseWire wire) {
        List<CommissionReturn> returns = new ArrayList<>();

        if (wire != null && wire.retornosComissao != null) {
            for (CommissionReturnWire item : wire.retornosComissao)
                returns.add(toPublic(item));
        }

        return returns;
    }

    public static CommissionReturn toPublic(CommissionReturnWire wire) {
        return new CommissionReturn(
                CommissionReturnType.fromWireName(wire.tipo),
                CommissionReturnTarget.fromWireName(wire.alvo),
                wire.retornoMinimo,
                wire.retornoMaximo,
                wire.permiteTaxaPadrao);
    }

    public static List<SimulationOption> toPublic(SimulationListResponseWire wire) {
        List<SimulationOption> options = new ArrayList<>();

        if (wire != null && wire.simulacoes != null) {
            for (SimulationOptionWire option : wire.simulacoes)
                options.add(toPublic(option));
        }

        return options;
    }

    public static SimulationOption toPublic(SimulationOptionWire wire) {
        return new SimulationOption(
                wire.idSimulacao,
                wire.valorSolicitado,
                wire.prazo,
                wire.valorParcela,
                wire.valorTac,
                wire.valorBoleto,
                wire.valorSeguro,
                wire.valorIof,
                wire.percentualIof,
                wire.valorCreditoTotal,
                wire.valorDividaTotal,
                wire.percentualCetMensal,
                wire.percentualCetAnual,
                wire.percentualJurosMensal,
                wire.percentualJurosAnual,
                wire.valorRetencaoDiariaAproximado);
    }

    public static Simulation toPublic(SimulationWire wire) {
        return new Simulation(
                wire.idSimulacao,
                wire.prazo,
                wire.valorParcela,
                wire.valorSolicitado,
                wire.valorEntrada,
                wire.valorBem,
                wire.valorTac,
                wire.taxaJurosNegociada,
                wire.valorRegistro,
                wire.uf,
                EnumMapping.fromWireName(SimulationType.class, wire.tipo),
                toPublic(wire.transacao),
                toPublic(wire.juros),
                toPublic(wire.cet),
                toPublic(wire.iof),
                toPublic(wire.veiculo),
                wire.cliente != null ? wire.cliente.cpf : null,
                wire.dataCriacao);
    }

    private static SimulationTransaction toPublic(TransactionWire wire) {
        return wire != null ? new SimulationTransaction(wire.valorTotalCredito, wire.valorTotalDebito) : null;
    }

    private static SimulationRate toPublic(InterestWire wire) {
        return wire != null ? new SimulationRate(wire.percentualJurosMensal, wire.percentualJurosAnual) : null;
    }

    private static SimulationRate toPublic(CetWire wire) {
        return wire != null ? new SimulationRate(wire.percentualMensal, wire.percentualAnual) : null;
    }

    private static SimulationIof toPublic(IofWire wire) {
        return wire != null ? new SimulationIof(wire.valor, wire.percentual) : null;
    }

    private static SimulationVehicle toPublic(VehicleWire wire) {
        if (wire == null)
            return null;

        return new SimulationVehicle(
                wire.marca, wire.modelo, wire.versao, wire.ano, wire.valor, wire.placa,
                wire.veiculoZeroKm, wire.chassi, wire.renavam, wire.cor);
    }
}
