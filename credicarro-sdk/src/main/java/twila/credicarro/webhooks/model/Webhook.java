package twila.credicarro.webhooks.model;

import java.util.UUID;
import lombok.Value;

/**
 * Webhook cadastrado (a credencial e o segredo de assinatura não são devolvidos pela listagem).
 */
@Value
public class Webhook {
    UUID id;
    WebhookType type;
    String url;
    WebhookAuthenticationType authenticationType;
    WebhookStatus status;
}
