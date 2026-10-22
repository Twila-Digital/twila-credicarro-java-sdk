package twila.credicarro.internal.generated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Envelope das listagens; o tipo dos itens é informado na leitura.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PagedResultWire<T> {

    @JsonProperty("itens")
    public List<T> items;

    @JsonProperty("pagina")
    public PageWire page;
}
