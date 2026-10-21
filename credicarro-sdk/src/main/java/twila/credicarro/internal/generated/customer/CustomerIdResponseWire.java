package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Resposta de POST v1/customer e v1/customer/unified.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CustomerIdResponseWire {

    @JsonProperty("id_cliente")
    public UUID idCliente;
}
