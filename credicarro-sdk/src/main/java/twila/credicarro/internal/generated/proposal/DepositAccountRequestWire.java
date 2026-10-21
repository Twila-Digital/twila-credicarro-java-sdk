package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de POST v2/proposal/{id}/deposit-account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DepositAccountRequestWire {

    @JsonProperty("cpfTitular")
    public String cpfTitular;

    @JsonProperty("codigoBanco")
    public String codigoBanco;

    @JsonProperty("agencia")
    public String agencia;

    @JsonProperty("digitoAgencia")
    public String digitoAgencia;

    @JsonProperty("conta")
    public String conta;

    @JsonProperty("digitoConta")
    public String digitoConta;

    @JsonProperty("tipoConta")
    public String tipoConta;
}
