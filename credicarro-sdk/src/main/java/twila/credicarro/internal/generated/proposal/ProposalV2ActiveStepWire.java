package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema ProposalV2ActiveStep.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2ActiveStepWire {

    @JsonProperty("enum_etapa")
    public Integer enumEtapa;

    @JsonProperty("etapa")
    public String etapa;

    @JsonProperty("enum_situacao")
    public Integer enumSituacao;

    @JsonProperty("situacao")
    public String situacao;

    @JsonProperty("enum_responsavel_acao")
    public Integer enumResponsavelAcao;

    @JsonProperty("responsavel_acao")
    public String responsavelAcao;
}
