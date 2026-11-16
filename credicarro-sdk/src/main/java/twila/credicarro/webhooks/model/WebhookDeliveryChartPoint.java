package twila.credicarro.webhooks.model;

import lombok.Value;

/**
 * Contagem de envios em um intervalo do gráfico, no horário de Brasília.
 */
@Value
public class WebhookDeliveryChartPoint {
    /** Início do intervalo ({@code rotulo}): {@code 10m} (10 minutos), {@code 14h} (hora) ou {@code 05} (dia). */
    String label;
    /** Envios com sucesso ({@code sucessos}). */
    int successCount;
    /** Envios com erro ({@code erros}). */
    int errorCount;
}
