package twila.credicarro.webhooks;

import java.util.Objects;
import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.internal.generated.webhook.CreateWebhookResponseWire;
import twila.credicarro.internal.generated.webhook.WebhookWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.WebhookMapper;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;

public final class WebhooksClientImpl implements WebhooksClient {

    private final ApiRequestExecutor executor;

    public WebhooksClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public CreateWebhookResult create(CreateWebhookRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");
        Objects.requireNonNull(request.getType(), "request.type é obrigatório.");
        Objects.requireNonNull(request.getAuthenticationType(), "request.authenticationType é obrigatório.");

        ApiResponse response = executor.post(Routes.WEBHOOKS, JsonSupport.write(WebhookMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        CreateWebhookResponseWire wire = JsonSupport.read(response, CreateWebhookResponseWire.class);
        return new CreateWebhookResult(wire.idWebhook, wire.segredoAssinatura);
    }

    @Override
    public PagedResult<Webhook> list(ListRequest request) {
        ApiResponse response = executor.get(JsonSupport.pagedPath(Routes.WEBHOOKS, request));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.readPaged(response, WebhookWire.class, WebhookMapper::toPublic);
    }

    @Override
    public void update(UUID webhookId, UpdateWebhookRequest request) {
        Objects.requireNonNull(webhookId, "webhookId é obrigatório.");
        Objects.requireNonNull(request, "request é obrigatório.");
        Objects.requireNonNull(request.getType(), "request.type é obrigatório.");

        ApiResponse response = executor.put(Routes.format(Routes.WEBHOOK_BY_ID, webhookId), JsonSupport.write(WebhookMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);
    }

    @Override
    public void delete(UUID webhookId) {
        Objects.requireNonNull(webhookId, "webhookId é obrigatório.");

        ApiResponse response = executor.delete(Routes.format(Routes.WEBHOOK_BY_ID, webhookId));
        ApiRequestExecutor.ensureSuccess(response);
    }
}
