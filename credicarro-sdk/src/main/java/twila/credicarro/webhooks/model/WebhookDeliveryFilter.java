package twila.credicarro.webhooks.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * Filtros do histórico de envios. Campos {@code null} não são enviados.
 */
@Value
@Builder(toBuilder = true)
public class WebhookDeliveryFilter {
    /** Início do período ({@code data_inicial}). */
    OffsetDateTime initialDate;
    /** Fim do período ({@code data_final}). */
    OffsetDateTime finalDate;
    /** {@code id_proposta}. */
    UUID proposalId;
    /** {@code id_simulacao}. */
    UUID simulationId;
    /** {@code numero_proposta}, até 50 caracteres. */
    String proposalNumber;
    /** Status HTTP devolvido pela sua URL, de 100 a 599 ({@code status_http}). */
    Integer statusCode;

    public static WebhookDeliveryFilter none() {
        return builder().build();
    }
}
