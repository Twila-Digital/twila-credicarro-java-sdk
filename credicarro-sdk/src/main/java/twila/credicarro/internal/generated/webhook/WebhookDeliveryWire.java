package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item de GET v1/webhook/audit.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookDeliveryWire {

    @JsonProperty("id_auditoria")
    public UUID idAuditoria;

    @JsonProperty("id_webhook")
    public UUID idWebhook;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("requisicao")
    public String requisicao;

    @JsonProperty("resposta")
    public String resposta;

    @JsonProperty("status_http")
    public Integer statusHttp;

    @JsonProperty("data_criacao")
    public OffsetDateTime dataCriacao;
}
