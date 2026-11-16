package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta de POST v1/webhook/audit/{id}/resend.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookResendResponseWire {

    @JsonProperty("status_http")
    public Integer statusHttp;

    @JsonProperty("resposta")
    public String resposta;
}
