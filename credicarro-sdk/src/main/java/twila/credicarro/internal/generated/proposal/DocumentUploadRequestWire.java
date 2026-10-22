package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de POST v2/proposal/{id}/document.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DocumentUploadRequestWire {

    @JsonProperty("participante")
    public String participante;

    @JsonProperty("tipoDocumento")
    public int tipoDocumento;

    @JsonProperty("nomeArquivo")
    public String nomeArquivo;

    @JsonProperty("conteudoBase64")
    public String conteudoBase64;
}
