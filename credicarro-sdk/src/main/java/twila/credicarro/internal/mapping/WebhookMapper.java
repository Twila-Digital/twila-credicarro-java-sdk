package twila.credicarro.internal.mapping;

import twila.credicarro.internal.generated.webhook.CreateWebhookRequestWire;
import twila.credicarro.internal.generated.webhook.ProposalWebhookEventWire;
import twila.credicarro.internal.generated.webhook.UpdateWebhookRequestWire;
import twila.credicarro.internal.generated.webhook.WebhookWire;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.webhooks.model.CreateWebhookRequest;
import twila.credicarro.webhooks.model.ProposalEventReason;
import twila.credicarro.webhooks.model.ProposalWebhookEvent;
import twila.credicarro.webhooks.model.UpdateWebhookRequest;
import twila.credicarro.webhooks.model.Webhook;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookStatus;
import twila.credicarro.webhooks.model.WebhookType;

public final class WebhookMapper {

    private WebhookMapper() {
    }

    public static CreateWebhookRequestWire toWire(CreateWebhookRequest request) {
        CreateWebhookRequestWire wire = new CreateWebhookRequestWire();
        wire.tipo = request.getType().wireValue();
        wire.url = request.getUrl();
        wire.tipoAutenticacao = request.getAuthenticationType().wireValue();
        wire.credencial = request.getCredential();
        return wire;
    }

    public static UpdateWebhookRequestWire toWire(UpdateWebhookRequest request) {
        UpdateWebhookRequestWire wire = new UpdateWebhookRequestWire();
        wire.tipo = request.getType().wireValue();
        wire.url = request.getUrl();
        wire.status = request.getStatus() != null ? request.getStatus().wireValue() : null;
        return wire;
    }

    public static Webhook toPublic(WebhookWire wire) {
        return new Webhook(
                wire.idWebhook,
                WebhookType.fromName(wire.tipo),
                wire.url,
                WebhookAuthenticationType.fromName(wire.tipoAutenticacao),
                WebhookStatus.fromName(wire.status));
    }

    public static ProposalWebhookEvent toPublic(ProposalWebhookEventWire wire) {
        ProposalStatus status = ProposalMapper.statusOf(wire.enumStatus, wire.status);

        boolean detailed = wire.motivoEvento != null
                || wire.dataEvento != null
                || wire.comprador != null
                || wire.vendedor != null
                || wire.loja != null;

        return new ProposalWebhookEvent(
                wire.idProposta,
                status,
                wire.enumStatus != null ? wire.enumStatus : status.wireValue(),
                wire.status,
                wire.motivoEvento != null ? EnumMapping.fromWireName(ProposalEventReason.class, wire.motivoEvento) : null,
                wire.motivoEvento,
                wire.motivoRecusa,
                wire.dataEvento,
                ProposalMapper.toPublic(wire.comprador),
                ProposalMapper.toPublic(wire.vendedor),
                ProposalMapper.toPublic(wire.loja),
                detailed);
    }
}
