package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Schema Interest.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class InterestWire {

    @JsonProperty("percentual_juros_mensal")
    public BigDecimal percentualJurosMensal;

    @JsonProperty("percentual_juros_anual")
    public BigDecimal percentualJurosAnual;
}
