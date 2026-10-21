package twila.credicarro.proposals.model;

import java.util.List;
import lombok.Value;

/**
 * Ação pendente do integrador em um participante da proposta.
 */
@Value
public class PendingAction {
    ProposalParticipant participant;
    ProposalStep step;
    PendingActionKind kind;
    List<DocumentType> documentTypes;
}
