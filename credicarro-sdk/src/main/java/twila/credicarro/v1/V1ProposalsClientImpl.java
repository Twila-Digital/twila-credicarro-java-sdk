package twila.credicarro.v1;

import java.util.Objects;
import java.util.UUID;
import twila.credicarro.internal.generated.proposal.ProposalIdResponseWire;
import twila.credicarro.internal.generated.proposal.ProposalWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.ProposalMapper;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.ProposalSummary;

public final class V1ProposalsClientImpl implements V1ProposalsClient {

    private final ApiRequestExecutor executor;

    public V1ProposalsClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public UUID create(CreateProposalRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V1_PROPOSALS, JsonSupport.write(ProposalMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.read(response, ProposalIdResponseWire.class).idProposta;
    }

    @Override
    public ProposalSummary get(UUID proposalId) {
        Objects.requireNonNull(proposalId, "proposalId é obrigatório.");

        ApiResponse response = executor.get(Routes.format(Routes.V1_PROPOSAL_BY_ID, proposalId));
        ApiRequestExecutor.ensureSuccess(response);

        return ProposalMapper.toPublic(JsonSupport.read(response, ProposalWire.class));
    }
}
