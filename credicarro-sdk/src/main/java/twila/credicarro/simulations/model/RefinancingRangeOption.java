package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

/**
 * Faixa de valor refinanciável para um prazo.
 */
@Value
public class RefinancingRangeOption {
    int term;
    BigDecimal minimumAmount;
    BigDecimal maximumAmount;
}
