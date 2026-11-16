package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Corpo de POST v1/proposal/simulation/refinancing/without-cpf.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateRefinancingWithoutCpfRequestWire {

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("valorSolicitado")
    public BigDecimal valorSolicitado;

    @JsonProperty("uf")
    public String uf;
}
