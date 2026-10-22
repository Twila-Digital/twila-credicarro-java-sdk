package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de POST v2/proposal/{id}/vehicle-detail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class VehicleDetailRequestWire {

    @JsonProperty("renavam")
    public String renavam;

    @JsonProperty("chassi")
    public String chassi;

    @JsonProperty("placa")
    public String placa;

    @JsonProperty("cor")
    public String cor;
}
