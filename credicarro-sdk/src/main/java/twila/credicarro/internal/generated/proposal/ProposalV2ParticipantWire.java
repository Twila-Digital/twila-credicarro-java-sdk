package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Schema ProposalV2Participant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2ParticipantWire {

    @JsonProperty("cpf")
    public String cpf;

    @JsonProperty("enum_biometria")
    public Integer enumBiometria;

    @JsonProperty("biometria")
    public String biometria;

    @JsonProperty("status_ativo")
    public ProposalV2ActiveStepWire statusAtivo;

    @JsonProperty("etapas_concluidas")
    public List<ProposalV2CompletedStepWire> etapasConcluidas;

    @JsonProperty("documentos")
    public List<ProposalV2DocumentWire> documentos;
}
