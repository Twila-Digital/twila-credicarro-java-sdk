package twila.credicarro.webhooks.model;

import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Resultado imediato do reenvio.
 */
@Value
public class WebhookResendResult {
    /** Status HTTP devolvido pela sua URL; {@code null} quando ela não respondeu ({@code status_http}). */
    Integer statusCode;
    /** Corpo devolvido pela sua URL ({@code resposta}). */
    String response;

    /**
     * O corpo da resposta aparece mascarado.
     */
    @Override
    public String toString() {
        return Masking.describe("WebhookResendResult", "statusCode", statusCode, "response", Masking.mask(response));
    }
}
