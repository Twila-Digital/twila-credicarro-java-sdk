package twila.credicarro.webhooks.model;

import java.util.UUID;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Resultado do cadastro. O {@code signingSecret} só é devolvido nesta resposta: guarde-o com segurança.
 */
@Value
public class CreateWebhookResult {
    UUID webhookId;
    String signingSecret;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateWebhookResult", "webhookId", webhookId, "signingSecret", Masking.mask(signingSecret));
    }
}
