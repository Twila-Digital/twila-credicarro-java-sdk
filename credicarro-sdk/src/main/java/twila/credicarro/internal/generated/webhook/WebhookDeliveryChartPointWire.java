package twila.credicarro.internal.generated.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Item de GET v1/webhook/audit/chart.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WebhookDeliveryChartPointWire {

    @JsonProperty("rotulo")
    public String rotulo;

    @JsonProperty("sucessos")
    public int sucessos;

    @JsonProperty("erros")
    public int erros;
}
