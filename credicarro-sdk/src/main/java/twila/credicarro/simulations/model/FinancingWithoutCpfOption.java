package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

/**
 * Opção da simulação de financiamento sem CPF (uma por prazo). Não tem {@code simulationId}: nada é gravado e a opção
 * não serve para criar uma proposta.
 */
@Value
public class FinancingWithoutCpfOption {
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
    BigDecimal minimumDownPayment;
}
