package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Resposta de GET .../range/refinancing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RefinancingRangeResponseWire {

    @JsonProperty("opcoes_por_prazo")
    public List<RefinancingRangeOptionWire> opcoesPorPrazo;
}
