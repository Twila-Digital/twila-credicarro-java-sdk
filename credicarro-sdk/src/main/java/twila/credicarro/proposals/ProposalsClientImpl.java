package twila.credicarro.proposals;

import java.util.Objects;
import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.internal.generated.proposal.ProposalIdResponseWire;
import twila.credicarro.internal.generated.proposal.ProposalV2Wire;
import twila.credicarro.internal.generated.proposal.ProposalWire;
import twila.credicarro.internal.http.ApiRequestExecutor;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.JsonSupport;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.mapping.ProposalMapper;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.DepositAccount;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.VehicleDetails;

public final class ProposalsClientImpl implements ProposalsClient {

    private final ApiRequestExecutor executor;

    public ProposalsClientImpl(ApiRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public UUID create(CreateProposalRequest request) {
        Objects.requireNonNull(request, "request é obrigatório.");

        ApiResponse response = executor.post(Routes.V2_PROPOSALS, JsonSupport.write(ProposalMapper.toWire(request)));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.read(response, ProposalIdResponseWire.class).idProposta;
    }

    @Override
    public ProposalDetails get(UUID proposalId) {
        Objects.requireNonNull(proposalId, "proposalId é obrigatório.");

        ApiResponse response = executor.get(Routes.format(Routes.V2_PROPOSAL_BY_ID, proposalId));
        ApiRequestExecutor.ensureSuccess(response);

        return ProposalMapper.toPublic(JsonSupport.read(response, ProposalV2Wire.class));
    }

    @Override
    public PagedResult<ProposalSummary> list(ListRequest request) {
        ApiResponse response = executor.get(JsonSupport.pagedPath(Routes.V1_PROPOSALS, request));
        ApiRequestExecutor.ensureSuccess(response);

        return JsonSupport.readPaged(response, ProposalWire.class, ProposalMapper::toPublic);
    }

    @Override
    public void sendVehicleDetails(UUID proposalId, VehicleDetails details) {
        Objects.requireNonNull(proposalId, "proposalId é obrigatório.");
        Objects.requireNonNull(details, "details é obrigatório.");

        ApiResponse response = executor.post(Routes.format(Routes.V2_PROPOSAL_VEHICLE_DETAIL, proposalId),
                JsonSupport.write(ProposalMapper.toWire(details)));
        ApiRequestExecutor.ensureSuccess(response);
    }

    @Override
    public void sendDepositAccount(UUID proposalId, DepositAccount account) {
        Objects.requireNonNull(proposalId, "proposalId é obrigatório.");
        Objects.requireNonNull(account, "account é obrigatório.");

        ApiResponse response = executor.post(Routes.format(Routes.V2_PROPOSAL_DEPOSIT_ACCOUNT, proposalId),
                JsonSupport.write(ProposalMapper.toWire(account)));
        ApiRequestExecutor.ensureSuccess(response);
    }

    @Override
    public void uploadDocument(UUID proposalId, ProposalDocumentUpload upload) {
        uploadDocument(proposalId, upload, null);
    }

    @Override
    public void uploadDocument(UUID proposalId, ProposalDocumentUpload upload, String idempotencyKey) {
        Objects.requireNonNull(proposalId, "proposalId é obrigatório.");
        Objects.requireNonNull(upload, "upload é obrigatório.");
        Objects.requireNonNull(upload.getParticipant(), "upload.participant é obrigatório.");
        Objects.requireNonNull(upload.getDocumentType(), "upload.documentType é obrigatório.");
        Objects.requireNonNull(upload.getFile(), "upload.file é obrigatório.");

        ApiResponse response = executor.postDocument(Routes.format(Routes.V2_PROPOSAL_DOCUMENT, proposalId),
                JsonSupport.write(ProposalMapper.toWire(upload)), idempotencyKey);
        ApiRequestExecutor.ensureSuccess(response);
    }
}
