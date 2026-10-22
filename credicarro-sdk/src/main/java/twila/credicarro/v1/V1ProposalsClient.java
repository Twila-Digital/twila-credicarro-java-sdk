package twila.credicarro.v1;

import java.util.UUID;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.ProposalSummary;

/**
 * Propostas v1 (para simulações feitas sem {@code cnpjLoja}).
 */
public interface V1ProposalsClient {

    /**
     * {@code POST v1/proposal}.
     */
    UUID create(CreateProposalRequest request);

    /**
     * {@code GET v1/proposal/:id}.
     */
    ProposalSummary get(UUID proposalId);
}
