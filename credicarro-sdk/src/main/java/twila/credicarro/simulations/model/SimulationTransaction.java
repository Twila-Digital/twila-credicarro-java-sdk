package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class SimulationTransaction {
    BigDecimal totalCredit;
    BigDecimal totalDebit;
}
