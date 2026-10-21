package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Corpo de POST v2/proposal/simulation/refinancing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateRefinancingSimulationRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("valorSolicitado")
    public BigDecimal valorSolicitado;

    @JsonProperty("cnpjLoja")
    public String cnpjLoja;
}
