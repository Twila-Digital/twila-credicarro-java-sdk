package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Opcao de faixa de financiamento.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FinancingRangeOptionWire {

    @JsonProperty("prazo")
    public int prazo;

    @JsonProperty("valor_minimo_entrada")
    public BigDecimal valorMinimoEntrada;
}
