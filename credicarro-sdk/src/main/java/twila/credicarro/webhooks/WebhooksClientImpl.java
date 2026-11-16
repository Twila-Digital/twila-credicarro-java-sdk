package twila.credicarro.webhooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.internal.generated.webhook.CreateWebhookResponseWire;
import twila.credicarro.internal.generated.webhook.WebhookDeliveryChartPointWire;
import twila.credicarro.internal.generated.webhook.WebhookDeliveryWire;
import twila.credicarro.internal.generated.webhook.WebhookResendResponseWire;
import twila.credicarro.internal.generated.webhook.WebhookWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.QueryStringBuilder;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.WebhookMapper;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.CreateWebhookResult;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;
import twila.credicarro.webhooks.model.WebhookDelivery;
import twila.credicarro.webhooks.model.WebhookDeliveryChartPoint;
import twila.credicarro.webhooks.model.WebhookDeliveryFilter;
import twila.credicarro.webhooks.model.WebhookResendResult;

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

    @Override
    public PagedResult<WebhookDelivery> listDeliveries(WebhookDeliveryFilter filter, ListRequest paging) {
        ListRequest effective = paging != null ? paging : ListRequest.defaults();
        String path = WebhookMapper.addFilter(new QueryStringBuilder(), filter)
                .add("pagina_inicial", effective.getPage())
                .add("limite", effective.getPageSize())
                .build(Routes.WEBHOOK_AUDIT);
        ApiResponse response = executor.get(path);
        ApiRequestExecutor.ensureSuccess(response);
        return JsonSupport.readPaged(response, WebhookDeliveryWire.class, WebhookMapper::toPublic);
    }

    @Override
    public List<WebhookDeliveryChartPoint> getDeliveryChart(WebhookDeliveryFilter filter) {
        Objects.requireNonNull(filter, "filter é obrigatório.");
        Objects.requireNonNull(filter.getInitialDate(), "filter.initialDate é obrigatório.");
        Objects.requireNonNull(filter.getFinalDate(), "filter.finalDate é obrigatório.");
        ApiResponse response = executor.get(WebhookMapper.addFilter(new QueryStringBuilder(), filter).build(Routes.WEBHOOK_AUDIT_CHART));
        ApiRequestExecutor.ensureSuccess(response);

        List<WebhookDeliveryChartPoint> points = new ArrayList<>();
        for (WebhookDeliveryChartPointWire point : JsonSupport.read(response, WebhookDeliveryChartPointWire[].class))
            points.add(WebhookMapper.toPublic(point));
        return points;
    }

    @Override
    public WebhookResendResult resend(UUID deliveryId) {
        Objects.requireNonNull(deliveryId, "deliveryId é obrigatório.");
        ApiResponse response = executor.postWebhookResend(Routes.format(Routes.WEBHOOK_AUDIT_RESEND, deliveryId));
        ApiRequestExecutor.ensureSuccess(response);
        return WebhookMapper.toPublic(JsonSupport.read(response, WebhookResendResponseWire.class));
    }
}
