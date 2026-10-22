package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Schema ProposalV2CompletedStep.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2CompletedStepWire {

    @JsonProperty("enum_etapa")
    public Integer enumEtapa;

    @JsonProperty("etapa")
    public String etapa;

    @JsonProperty("data")
    public OffsetDateTime data;
}
