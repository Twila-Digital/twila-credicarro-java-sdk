package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

/**
 * Taxa mensal e anual (juros ou CET).
 */
@Value
public class SimulationRate {
    BigDecimal monthlyRate;
    BigDecimal annualRate;
}
