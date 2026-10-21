package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

/**
 * Valor mínimo de entrada para um prazo (financiamento).
 */
@Value
public class FinancingRangeOption {
    int term;
    BigDecimal minimumDownPayment;
}
