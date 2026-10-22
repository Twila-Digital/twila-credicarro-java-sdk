package twila.credicarro.webhooks.model;

import lombok.Builder;
import lombok.Value;

/**
 * Edição de webhook. {@code status} é opcional: quando omitido, a API mantém o status atual.
 */
@Value
@Builder
public class UpdateWebhookRequest {
    WebhookType type;
    String url;
    WebhookStatus status;
}
