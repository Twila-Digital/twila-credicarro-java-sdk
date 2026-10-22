package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Schema Cet.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CetWire {

    @JsonProperty("percentual_mensal")
    public BigDecimal percentualMensal;

    @JsonProperty("percentual_anual")
    public BigDecimal percentualAnual;
}
