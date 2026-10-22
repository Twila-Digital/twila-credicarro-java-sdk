package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Schema WebhookResponse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookWire {

    @JsonProperty("id_webhook")
    public UUID idWebhook;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("url")
    public String url;

    @JsonProperty("tipo_autenticacao")
    public String tipoAutenticacao;

    @JsonProperty("status")
    public String status;
}
