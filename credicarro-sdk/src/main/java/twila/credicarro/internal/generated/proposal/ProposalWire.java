package twila.credicarro.internal.generated.proposal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Schema ProposalResponse (v1).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ProposalWire {

    @JsonProperty("id_proposta")
    public UUID idProposta;

    @JsonProperty("numero")
    public Long numero;

    @JsonProperty("id_simulacao")
    public UUID idSimulacao;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("status")
    public String status;

    @JsonProperty("comprador")
    public PartyWire comprador;

    @JsonProperty("vendedor")
    public PartyWire vendedor;

    @JsonProperty("data_criacao")
    public OffsetDateTime dataCriacao;
}
