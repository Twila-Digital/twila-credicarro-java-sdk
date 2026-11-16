package twila.credicarro.webhooks;

import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;
import twila.credicarro.webhooks.model.WebhookDelivery;
import twila.credicarro.webhooks.model.WebhookDeliveryFilter;
import twila.credicarro.webhooks.model.WebhookResendResult;

/**
 * Gerenciamento dos webhooks do parceiro. É permitido mais de um webhook por tipo.
 */
public interface WebhooksClient {

    /**
     * Cadastra um webhook ativo ({@code POST v1/webhook}). O {@code signingSecret} só é devolvido aqui: guarde-o. Esta
     * chamada não aceita {@code Idempotency-Key} e por isso o SDK <b>não</b> a repete automaticamente.
     */
    CreateWebhookResult create(CreateWebhookRequest request);

    default PagedResult<Webhook> list() {
        return list(ListRequest.defaults());
    }

    /**
     * Lista os webhooks de forma paginada ({@code GET v1/webhook}).
     */
    PagedResult<Webhook> list(ListRequest request);

    /**
     * Atualiza tipo, URL e status ({@code PUT v1/webhook/:id}, {@code 204}); a autenticação não pode ser alterada.
     */
    void update(UUID webhookId, UpdateWebhookRequest request);

    /**
     * Exclui o webhook ({@code DELETE v1/webhook/:id}, {@code 204}).
     */
    void delete(UUID webhookId);

    default PagedResult<WebhookDelivery> listDeliveries() {
        return listDeliveries(WebhookDeliveryFilter.none(), ListRequest.defaults());
    }

    /**
     * Histórico de envios, com o corpo enviado, a resposta da sua URL e o status HTTP ({@code GET v1/webhook/audit}).
     * Todos os filtros são opcionais.
     */
    PagedResult<WebhookDelivery> listDeliveries(WebhookDeliveryFilter filter, ListRequest paging);

    /**
     * Reenvia para a sua URL o mesmo corpo de um envio do histórico ({@code POST v1/webhook/audit/:id/resend}). É uma
     * única tentativa, <b>não</b> repetida automaticamente (repetir dispararia o webhook de novo), com até 30 s de espera
     * pela sua URL. O reenvio chega com uma {@code Idempotency-Key} nova ({@code {id_mensagem}:{id_webhook}:{id_tentativa}}).
     */
    WebhookResendResult resend(UUID deliveryId);
}
