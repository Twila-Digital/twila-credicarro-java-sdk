package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Opcao de faixa de refinanciamento.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RefinancingRangeOptionWire {

    @JsonProperty("prazo")
    public int prazo;

    @JsonProperty("valor_minimo_refinanciavel")
    public BigDecimal valorMinimoRefinanciavel;

    @JsonProperty("valor_maximo_refinanciavel")
    public BigDecimal valorMaximoRefinanciavel;
}
