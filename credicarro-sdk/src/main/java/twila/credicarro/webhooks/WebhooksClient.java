package twila.credicarro.webhooks;

import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;

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
}
