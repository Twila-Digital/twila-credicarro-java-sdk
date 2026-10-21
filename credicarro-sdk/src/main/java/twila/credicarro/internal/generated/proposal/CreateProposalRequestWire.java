package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Corpo de POST v1|v2/proposal.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateProposalRequestWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("simulacaoId")
    public UUID simulacaoId;
}
