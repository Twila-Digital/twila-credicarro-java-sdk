package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schemas Buyer e Seller (cpf).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PartyWire {

    @JsonProperty("cpf")
    public String cpf;
}
