package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Schema Vehicle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class VehicleWire {

    @JsonProperty("marca")
    public String marca;

    @JsonProperty("modelo")
    public String modelo;

    @JsonProperty("versao")
    public String versao;

    @JsonProperty("ano")
    public Integer ano;

    @JsonProperty("valor")
    public BigDecimal valor;

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("veiculo_zero_km")
    public Boolean veiculoZeroKm;

    @JsonProperty("chassi")
    public String chassi;

    @JsonProperty("renavam")
    public String renavam;

    @JsonProperty("cor")
    public String cor;
}
