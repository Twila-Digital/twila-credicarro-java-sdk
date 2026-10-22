package twila.credicarro.internal.generated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objeto pagina das listagens (schema Page).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PageWire {

    @JsonProperty("tem_proximo")
    public boolean hasNext;

    @JsonProperty("tem_anterior")
    public boolean hasPrevious;

    @JsonProperty("numero")
    public int number;

    @JsonProperty("tamanho")
    public int size;

    @JsonProperty("total")
    public long total;
}
