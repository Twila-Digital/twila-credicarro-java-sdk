package twila.credicarro.internal.generated.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Corpo de POST v1/customer/unified.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UpsertCustomerRequestWire {

    @JsonProperty("celular")
    public String celular;

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("dataDeNascimento")
    public OffsetDateTime dataDeNascimento;

    @JsonProperty("email")
    public String email;

    @JsonProperty("nome")
    public String nome;

    @JsonProperty("sobrenome")
    public String sobrenome;

    @JsonProperty("endereco")
    public CustomerAddressWire endereco;

    @JsonProperty("dadosProfissionais")
    public ProfessionalDataWire dadosProfissionais;
}
