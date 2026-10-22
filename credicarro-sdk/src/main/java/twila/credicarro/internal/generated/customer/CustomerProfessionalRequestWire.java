package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Corpo de POST v1/customer/professional.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CustomerProfessionalRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("tipoOcupacao")
    public int tipoOcupacao;

    @JsonProperty("renda")
    public BigDecimal renda;
}
