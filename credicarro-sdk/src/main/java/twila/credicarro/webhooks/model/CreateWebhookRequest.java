package twila.credicarro.webhooks.model;

import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Cadastro de webhook. {@code credential} é obrigatória para BASIC (Base64 de {@code usuario:senha}) e JWT (token);
 * a autenticação não pode ser alterada depois da criação.
 */
@Value
@Builder
public class CreateWebhookRequest {
    WebhookType type;
    String url;
    WebhookAuthenticationType authenticationType;
    String credential;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateWebhookRequest", "type", type, "url", url, "authenticationType", authenticationType, "credential", Masking.mask(credential));
    }
}
