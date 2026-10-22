package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;
import twila.credicarro.internal.generated.proposal.ProposalV2ParticipantWire;
import twila.credicarro.internal.generated.proposal.ProposalV2StoreWire;

/**
 * Payload do webhook de proposta (v1 com 3 campos; v2 com o retrato completo).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalWebhookEventWire {

    @JsonProperty("id_proposta")
    public UUID idProposta;

    @JsonProperty("enum_status")
    public Integer enumStatus;

    @JsonProperty("status")
    public String status;

    @JsonProperty("motivo_evento")
    public String motivoEvento;

    @JsonProperty("motivo_recusa")
    public String motivoRecusa;

    @JsonProperty("data_evento")
    public OffsetDateTime dataEvento;

    @JsonProperty("comprador")
    public ProposalV2ParticipantWire comprador;

    @JsonProperty("vendedor")
    public ProposalV2ParticipantWire vendedor;

    @JsonProperty("loja")
    public ProposalV2StoreWire loja;
}
