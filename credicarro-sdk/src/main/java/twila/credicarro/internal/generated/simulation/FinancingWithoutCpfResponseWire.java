package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Resposta de POST v1/proposal/simulation/financing/without-cpf.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FinancingWithoutCpfResponseWire {

    @JsonProperty("cotacoes")
    public List<FinancingWithoutCpfOptionWire> cotacoes;
}
