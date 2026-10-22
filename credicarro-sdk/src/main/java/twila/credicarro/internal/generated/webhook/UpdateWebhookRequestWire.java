package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de PUT v1/webhook/{id}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class UpdateWebhookRequestWire {

    @JsonProperty("tipo")
    public int tipo;

    @JsonProperty("url")
    public String url;

    @JsonProperty("status")
    public Integer status;
}
