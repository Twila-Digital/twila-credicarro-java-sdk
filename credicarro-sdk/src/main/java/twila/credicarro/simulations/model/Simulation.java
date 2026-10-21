package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Simulação consultada ({@code GET v1/proposal/simulation/{id}} e listagem; schema {@code SimulationResponse}).
 */
@Value
public class Simulation {
    UUID simulationId;
    Long term;
    BigDecimal installmentAmount;
    BigDecimal requestedAmount;
    BigDecimal downPayment;
    BigDecimal assetAmount;
    BigDecimal tacAmount;
    BigDecimal negotiatedInterestRate;
    BigDecimal registrationAmount;
    String state;
    SimulationType type;
    SimulationTransaction transaction;
    SimulationRate interest;
    SimulationRate cet;
    SimulationIof iof;
    SimulationVehicle vehicle;
    String customerCpf;
    OffsetDateTime createdAt;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("Simulation", "simulationId", simulationId, "term", term, "installmentAmount", installmentAmount, "requestedAmount", requestedAmount, "downPayment", downPayment, "assetAmount", assetAmount, "tacAmount", tacAmount, "state", state, "type", type, "transaction", transaction, "interest", interest, "cet", cet, "iof", iof, "vehicle", vehicle, "customerCpf", Masking.cpf(customerCpf), "createdAt", createdAt);
    }
}
