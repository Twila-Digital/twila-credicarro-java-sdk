package twila.credicarro.webhooks.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;
import twila.credicarro.internal.util.Masking;
import twila.credicarro.proposals.model.ProposalParticipantState;
import twila.credicarro.proposals.model.ProposalStatus;

/**
 * Evento do webhook de proposta. O payload v1 traz só {@code id_proposta}, {@code enum_status} e {@code status}
 * (inglês); o v2 (propostas criadas por {@code POST v2/proposal}) traz o retrato completo e {@code isDetailed} é true.
 */
@Value
public class ProposalWebhookEvent {
    UUID proposalId;
    ProposalStatus status;
    int statusRaw;
    String statusName;
    ProposalEventReason eventReason;
    String eventReasonRaw;
    String refusalReason;
    OffsetDateTime eventDate;
    ProposalParticipantState buyer;
    ProposalParticipantState seller;
    ProposalParticipantState store;
    boolean isDetailed;

    /**
     * Ações pendentes do integrador, quando o evento é detalhado (payload v2). Em eventos v1 a lista é vazia: consulte
     * a proposta com {@code proposals().get(id)}.
     */
    public java.util.List<twila.credicarro.proposals.model.PendingAction> getPendingActions() {
        return twila.credicarro.proposals.model.PendingActions.of(buyer, seller, store);
    }
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalWebhookEvent", "proposalId", proposalId, "status", status, "statusRaw", statusRaw, "statusName", statusName, "eventReason", eventReason, "eventReasonRaw", eventReasonRaw, "refusalReason", Masking.mask(refusalReason), "eventDate", eventDate, "buyer", buyer, "seller", seller, "store", store, "isDetailed", isDetailed);
    }
}
