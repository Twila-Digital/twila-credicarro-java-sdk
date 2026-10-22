package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de POST v1/customer/address.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CustomerAddressRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("endereco")
    public CustomerAddressWire endereco;
}
