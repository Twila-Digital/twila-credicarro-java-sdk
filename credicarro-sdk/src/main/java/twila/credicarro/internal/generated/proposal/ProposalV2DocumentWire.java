package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema ProposalV2Document.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2DocumentWire {

    @JsonProperty("enum_tipo")
    public Integer enumTipo;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("enum_status")
    public Integer enumStatus;

    @JsonProperty("status")
    public String status;

    @JsonProperty("motivo")
    public String motivo;
}
