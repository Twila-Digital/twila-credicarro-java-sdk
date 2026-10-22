package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Schema Transaction.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TransactionWire {

    @JsonProperty("valor_total_credito")
    public BigDecimal valorTotalCredito;

    @JsonProperty("valor_total_debito")
    public BigDecimal valorTotalDebito;
}
