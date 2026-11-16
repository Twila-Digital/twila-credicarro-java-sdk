package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Resposta de GET v2/proposal/simulation/commission-returns.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommissionReturnsResponseWire {

    @JsonProperty("retornos_comissao")
    public List<CommissionReturnWire> retornosComissao;
}
