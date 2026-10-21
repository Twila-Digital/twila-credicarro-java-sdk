package twila.credicarro.proposals.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Retrato atual da proposta ({@code GET v2/proposal/{id}}, schema ProposalV2Response): status, link de biometria e
 * as etapas e os documentos do comprador, da loja e, quando houver, do vendedor.
 */
@Value
public class ProposalDetails {
    UUID id;
    Long number;
    UUID simulationId;
    ProposalStatus status;
    int statusRaw;
    String statusName;
    ProposalType type;
    OffsetDateTime createdAt;
    String refusalReason;
    String biometryLink;
    ProposalParticipantState buyer;
    ProposalParticipantState seller;
    ProposalParticipantState store;

    /**
     * Ações que o integrador precisa executar agora (ver {@link PendingActions#of}).
     */
    public List<PendingAction> getPendingActions() {
        return PendingActions.of(buyer, seller, store);
    }

    /**
     * Sem o link de biometria (dá acesso à sessão do cliente) e com os participantes mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalDetails", "id", id, "number", number, "simulationId", simulationId, "status", status,
                "statusRaw", statusRaw, "statusName", statusName, "type", type, "createdAt", createdAt,
                "refusalReason", Masking.mask(refusalReason), "biometryLink", Masking.mask(biometryLink),
                "buyer", buyer, "seller", seller, "store", store);
    }
}
