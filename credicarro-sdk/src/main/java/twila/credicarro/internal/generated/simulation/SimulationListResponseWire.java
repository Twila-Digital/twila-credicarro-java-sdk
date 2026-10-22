package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Resposta de POST .../simulation/financing e refinancing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SimulationListResponseWire {

    @JsonProperty("simulacoes")
    public List<SimulationOptionWire> simulacoes;
}
