package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Item de GET v2/proposal/simulation/commission-returns.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommissionReturnWire {

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("alvo")
    public String alvo;

    @JsonProperty("retorno_minimo")
    public int retornoMinimo;

    @JsonProperty("retorno_maximo")
    public int retornoMaximo;

    @JsonProperty("permite_taxa_padrao")
    public boolean permiteTaxaPadrao;
}
