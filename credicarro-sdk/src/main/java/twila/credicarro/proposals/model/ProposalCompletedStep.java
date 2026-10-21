package twila.credicarro.proposals.model;

import java.time.OffsetDateTime;
import lombok.Value;

/**
 * Etapa concluída (data da última passagem pela etapa).
 */
@Value
public class ProposalCompletedStep {
    ProposalStep step;
    int stepRaw;
    String stepName;
    OffsetDateTime date;
}
