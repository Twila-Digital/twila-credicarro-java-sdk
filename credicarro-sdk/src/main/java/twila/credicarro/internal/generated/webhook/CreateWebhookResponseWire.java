package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Resposta de POST v1/webhook.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateWebhookResponseWire {

    @JsonProperty("id_webhook")
    public UUID idWebhook;

    @JsonProperty("segredo_assinatura")
    public String segredoAssinatura;
}
