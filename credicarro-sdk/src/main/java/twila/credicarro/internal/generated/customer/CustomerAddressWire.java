package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Endereco do cliente (endereco).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CustomerAddressWire {

    @JsonProperty("rua")
    public String rua;

    @JsonProperty("cidade")
    public String cidade;

    @JsonProperty("estado")
    public String estado;

    @JsonProperty("bairro")
    public String bairro;

    @JsonProperty("cep")
    public String cep;

    @JsonProperty("numero")
    public String numero;

    @JsonProperty("complemento")
    public String complemento;
}
