package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de POST v1/webhook.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CreateWebhookRequestWire {

    @JsonProperty("tipo")
    public int tipo;

    @JsonProperty("url")
    public String url;

    @JsonProperty("tipo_autenticacao")
    public int tipoAutenticacao;

    @JsonProperty("credencial")
    public String credencial;
}
