package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Resposta de POST v1|v2/proposal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalIdResponseWire {

    @JsonProperty("id_proposta")
    public UUID idProposta;
}
