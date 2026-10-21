package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Corpo de POST v1/proposal/simulation/financing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class V1CreateFinancingSimulationRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("isVeiculoZeroKm")
    public boolean isVeiculoZeroKm;

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("valorVeiculo")
    public BigDecimal valorVeiculo;

    @JsonProperty("uf")
    public String uf;

    @JsonProperty("valorEntrada")
    public BigDecimal valorEntrada;
}
