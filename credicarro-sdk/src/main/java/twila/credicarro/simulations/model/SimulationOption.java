package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;

/**
 * Opção de simulação (uma por prazo). {@code simulationId} é o {@code simulacaoId} usado ao criar a proposta.
 */
@Value
public class SimulationOption {
    UUID simulationId;
    BigDecimal requestedAmount;
    Integer term;
    BigDecimal installmentAmount;
    BigDecimal tacAmount;
    BigDecimal boletoAmount;
    BigDecimal insuranceAmount;
    BigDecimal iofAmount;
    BigDecimal iofRate;
    BigDecimal totalCreditAmount;
    BigDecimal totalDebtAmount;
    BigDecimal monthlyCet;
    BigDecimal annualCet;
    BigDecimal monthlyInterestRate;
    BigDecimal annualInterestRate;
    BigDecimal approximateDailyRetention;
}
