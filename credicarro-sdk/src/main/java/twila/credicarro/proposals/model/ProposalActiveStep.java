package twila.credicarro.proposals.model;

import lombok.Value;

/**
 * Etapa em que o participante está agora, a situação dela e quem precisa agir. Cada valor traz o enum, o código
 * bruto (preservado mesmo quando o enum é UNKNOWN) e o nome.
 */
@Value
public class ProposalActiveStep {
    ProposalStep step;
    int stepRaw;
    String stepName;
    StepSituation situation;
    int situationRaw;
    String situationName;
    ActionOwner actionOwner;
    int actionOwnerRaw;
    String actionOwnerName;
}
