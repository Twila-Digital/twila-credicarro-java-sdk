package twila.credicarro.proposals;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import twila.credicarro.proposals.model.ActionOwner;
import twila.credicarro.proposals.model.DocumentStatus;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.PendingAction;
import twila.credicarro.proposals.model.PendingActionKind;
import twila.credicarro.proposals.model.ProposalActiveStep;
import twila.credicarro.proposals.model.ProposalCompletedStep;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalDocumentState;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.proposals.model.ProposalParticipantState;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalStep;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.proposals.model.StepSituation;

class PendingActionsTest {

    private static ProposalActiveStep active(ProposalStep step, ActionOwner owner) {
        return new ProposalActiveStep(step, step.wireValue(), step.name(), StepSituation.AWAITING_SUBMISSION, 1, "AGUARDANDO_ENVIO",
                owner, owner.wireValue(), owner.name());
    }

    private static ProposalDocumentState document(DocumentType type, DocumentStatus status) {
        return new ProposalDocumentState(type, type.wireValue(), type.name(), status, status.wireValue(), status.name(), null);
    }

    private static ProposalParticipantState participant(ProposalActiveStep step, ProposalDocumentState... documents) {
        return new ProposalParticipantState("doc", null, null, null, step,
                Collections.<ProposalCompletedStep>emptyList(), Arrays.asList(documents));
    }

    private static ProposalDetails details(ProposalParticipantState buyer, ProposalParticipantState seller, ProposalParticipantState store) {
        return new ProposalDetails(null, 1L, null, ProposalStatus.APPROVED, 2, "APROVADA", ProposalType.FINANCING, null, null, null,
                buyer, seller, store);
    }

    @Test
    void documentSubmissionListsOnlyThePendingDocuments() {
        ProposalDetails details = details(
                participant(active(ProposalStep.DOCUMENT_SUBMISSION, ActionOwner.INTEGRATOR),
                        document(DocumentType.PROOF_OF_ADDRESS, DocumentStatus.PENDING),
                        document(DocumentType.CNH, DocumentStatus.VALID),
                        document(DocumentType.PROOF_OF_INCOME, DocumentStatus.PENDING)),
                null, null);

        List<PendingAction> actions = details.getPendingActions();

        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getParticipant()).isEqualTo(ProposalParticipant.BUYER);
        assertThat(actions.get(0).getStep()).isEqualTo(ProposalStep.DOCUMENT_SUBMISSION);
        assertThat(actions.get(0).getKind()).isEqualTo(PendingActionKind.SEND_DOCUMENTS);
        assertThat(actions.get(0).getDocumentTypes()).containsExactly(DocumentType.PROOF_OF_ADDRESS, DocumentType.PROOF_OF_INCOME);
    }

    @Test
    void documentCorrectionListsOnlyTheInvalidDocuments() {
        ProposalDetails details = details(
                participant(active(ProposalStep.DOCUMENT_CORRECTION, ActionOwner.INTEGRATOR),
                        document(DocumentType.CNH, DocumentStatus.INVALID),
                        document(DocumentType.PROOF_OF_ADDRESS, DocumentStatus.VALID)),
                null, null);

        PendingAction action = details.getPendingActions().get(0);

        assertThat(action.getKind()).isEqualTo(PendingActionKind.CORRECT_DOCUMENTS);
        assertThat(action.getDocumentTypes()).containsExactly(DocumentType.CNH);
    }

    @Test
    void vehicleDetailsAndDepositAccountHaveNoDocumentTypes() {
        ProposalDetails details = details(
                participant(active(ProposalStep.DEPOSIT_ACCOUNT, ActionOwner.INTEGRATOR)),
                null,
                participant(active(ProposalStep.VEHICLE_DETAILS, ActionOwner.INTEGRATOR)));

        List<PendingAction> actions = details.getPendingActions();

        assertThat(actions).hasSize(2);
        assertThat(actions.get(0).getParticipant()).isEqualTo(ProposalParticipant.BUYER);
        assertThat(actions.get(0).getKind()).isEqualTo(PendingActionKind.SEND_DEPOSIT_ACCOUNT);
        assertThat(actions.get(0).getDocumentTypes()).isEmpty();
        assertThat(actions.get(1).getParticipant()).isEqualTo(ProposalParticipant.STORE);
        assertThat(actions.get(1).getKind()).isEqualTo(PendingActionKind.SEND_VEHICLE_DETAILS);
    }

    @Test
    void ignoresParticipantsWhoseActionOwnerIsNotTheIntegrator() {
        ProposalDetails details = details(
                participant(active(ProposalStep.DOCUMENT_SUBMISSION, ActionOwner.CUSTOMER)),
                participant(active(ProposalStep.DOCUMENT_SUBMISSION, ActionOwner.CREDICARRO)),
                participant(active(ProposalStep.DOCUMENT_SUBMISSION, ActionOwner.NONE)));

        assertThat(details.getPendingActions()).isEmpty();
    }

    @Test
    void ignoresStepsThatNeedNoIntegratorAction() {
        ProposalDetails details = details(
                participant(active(ProposalStep.SIGNATURE, ActionOwner.INTEGRATOR)),
                participant(active(ProposalStep.BIOMETRY_ANALYSIS, ActionOwner.INTEGRATOR)),
                participant(active(ProposalStep.PAYMENT, ActionOwner.INTEGRATOR)));

        assertThat(details.getPendingActions()).isEmpty();
    }

    @Test
    void toleratesMissingParticipantsAndActiveSteps() {
        assertThat(details(null, null, null).getPendingActions()).isEmpty();
        assertThat(details(participant(null), null, null).getPendingActions()).isEmpty();
    }
}
