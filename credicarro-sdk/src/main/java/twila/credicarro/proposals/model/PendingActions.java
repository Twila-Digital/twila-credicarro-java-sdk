package twila.credicarro.proposals.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Regra das ações pendentes do integrador, compartilhada entre {@link ProposalDetails} e o evento detalhado do webhook.
 */
public final class PendingActions {

    private PendingActions() {
    }

    /**
     * Só participantes cujo responsável pela ação é {@link ActionOwner#INTEGRATOR}: etapa 6 gera SEND_DOCUMENTS
     * (documentos PENDING); 7 gera CORRECT_DOCUMENTS (documentos INVALID); 8 gera SEND_VEHICLE_DETAILS; 5 gera
     * SEND_DEPOSIT_ACCOUNT.
     */
    public static List<PendingAction> of(ProposalParticipantState buyer, ProposalParticipantState seller, ProposalParticipantState store) {
        List<PendingAction> actions = new ArrayList<>();

        add(actions, ProposalParticipant.BUYER, buyer);
        add(actions, ProposalParticipant.SELLER, seller);
        add(actions, ProposalParticipant.STORE, store);

        return Collections.unmodifiableList(actions);
    }

    private static void add(List<PendingAction> actions, ProposalParticipant participant, ProposalParticipantState state) {
        if (state == null || state.getActiveStep() == null)
            return;

        ProposalActiveStep active = state.getActiveStep();
        if (active.getActionOwner() != ActionOwner.INTEGRATOR)
            return;

        switch (active.getStep()) {
            case DOCUMENT_SUBMISSION:
                actions.add(new PendingAction(participant, active.getStep(), PendingActionKind.SEND_DOCUMENTS,
                        typesWithStatus(state, DocumentStatus.PENDING)));
                break;
            case DOCUMENT_CORRECTION:
                actions.add(new PendingAction(participant, active.getStep(), PendingActionKind.CORRECT_DOCUMENTS,
                        typesWithStatus(state, DocumentStatus.INVALID)));
                break;
            case VEHICLE_DETAILS:
                actions.add(new PendingAction(participant, active.getStep(), PendingActionKind.SEND_VEHICLE_DETAILS,
                        Collections.<DocumentType>emptyList()));
                break;
            case DEPOSIT_ACCOUNT:
                actions.add(new PendingAction(participant, active.getStep(), PendingActionKind.SEND_DEPOSIT_ACCOUNT,
                        Collections.<DocumentType>emptyList()));
                break;
            default:
                break;
        }
    }

    private static List<DocumentType> typesWithStatus(ProposalParticipantState state, DocumentStatus status) {
        List<DocumentType> types = new ArrayList<>();

        if (state.getDocuments() != null) {
            for (ProposalDocumentState document : state.getDocuments()) {
                if (document.getStatus() == status)
                    types.add(document.getType());
            }
        }

        return Collections.unmodifiableList(types);
    }
}
