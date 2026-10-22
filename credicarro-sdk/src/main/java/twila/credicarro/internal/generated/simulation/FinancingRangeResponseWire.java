package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Resposta de GET .../range/financing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FinancingRangeResponseWire {

    @JsonProperty("opcoes_por_prazo")
    public List<FinancingRangeOptionWire> opcoesPorPrazo;
}
