package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Corpo de POST v2/proposal/simulation/financing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateFinancingSimulationRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("valorVeiculo")
    public BigDecimal valorVeiculo;

    @JsonProperty("valorEntrada")
    public BigDecimal valorEntrada;

    @JsonProperty("uf")
    public String uf;

    @JsonProperty("isVeiculoZeroKm")
    public boolean isVeiculoZeroKm;

    @JsonProperty("cnpjLoja")
    public String cnpjLoja;
}
