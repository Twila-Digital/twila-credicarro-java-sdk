package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Schema Iof.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class IofWire {

    @JsonProperty("valor")
    public BigDecimal valor;

    @JsonProperty("percentual")
    public BigDecimal percentual;
}
