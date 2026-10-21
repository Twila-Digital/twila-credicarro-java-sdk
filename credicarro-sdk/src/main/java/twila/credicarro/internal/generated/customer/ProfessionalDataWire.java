package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Dados profissionais (dadosProfissionais).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProfessionalDataWire {

    @JsonProperty("tipoOcupacao")
    public int tipoOcupacao;

    @JsonProperty("renda")
    public BigDecimal renda;
}
