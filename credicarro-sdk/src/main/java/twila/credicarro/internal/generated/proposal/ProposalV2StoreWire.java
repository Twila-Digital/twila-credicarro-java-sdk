package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Schema ProposalV2Store.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2StoreWire {

    @JsonProperty("cnpj")
    public String cnpj;

    @JsonProperty("status_ativo")
    public ProposalV2ActiveStepWire statusAtivo;

    @JsonProperty("etapas_concluidas")
    public List<ProposalV2CompletedStepWire> etapasConcluidas;

    @JsonProperty("documentos")
    public List<ProposalV2DocumentWire> documentos;
}
