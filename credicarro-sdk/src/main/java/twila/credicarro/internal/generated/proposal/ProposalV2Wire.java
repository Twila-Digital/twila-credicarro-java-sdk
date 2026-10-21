package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Schema ProposalV2Response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalV2Wire {

    @JsonProperty("id_proposta")
    public UUID idProposta;

    @JsonProperty("numero")
    public Long numero;

    @JsonProperty("id_simulacao")
    public UUID idSimulacao;

    @JsonProperty("enum_status")
    public Integer enumStatus;

    @JsonProperty("status")
    public String status;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("data_criacao")
    public OffsetDateTime dataCriacao;

    @JsonProperty("motivo_recusa")
    public String motivoRecusa;

    @JsonProperty("link_biometria")
    public String linkBiometria;

    @JsonProperty("comprador")
    public ProposalV2ParticipantWire comprador;

    @JsonProperty("vendedor")
    public ProposalV2ParticipantWire vendedor;

    @JsonProperty("loja")
    public ProposalV2StoreWire loja;
}
