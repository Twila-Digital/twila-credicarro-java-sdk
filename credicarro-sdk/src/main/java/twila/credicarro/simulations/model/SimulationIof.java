package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class SimulationIof {
    BigDecimal amount;
    BigDecimal rate;
}
