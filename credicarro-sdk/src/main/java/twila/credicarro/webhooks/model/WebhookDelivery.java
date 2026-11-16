package twila.credicarro.webhooks.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Envio de webhook registrado no histórico. Use {@code id} ({@code id_auditoria}) em {@code WebhooksClient.resend}.
 */
@Value
public class WebhookDelivery {
    /** {@code id_auditoria}. */
    UUID id;
    /** {@code id_webhook}; pode vir nulo. */
    UUID webhookId;
    WebhookType type;
    /** Corpo enviado, como texto JSON ({@code requisicao}). */
    String request;
    /** Corpo devolvido pela sua URL; vazio quando não houve resposta ({@code resposta}). */
    String response;
    /** Status HTTP devolvido pela sua URL; {@code null} quando ela não respondeu ({@code status_http}). */
    Integer statusCode;
    /** Data e hora do envio ({@code data_criacao}). */
    OffsetDateTime createdAt;

    /**
     * Os corpos (que podem conter dados pessoais) aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("WebhookDelivery", "id", id, "webhookId", webhookId, "type", type, "statusCode", statusCode, "createdAt", createdAt, "request", Masking.mask(request), "response", Masking.mask(response));
    }
}
