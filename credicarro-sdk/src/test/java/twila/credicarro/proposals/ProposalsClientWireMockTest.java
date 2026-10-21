package twila.credicarro.proposals;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.errors.CredicarroApiException;
import twila.credicarro.proposals.model.ActionOwner;
import twila.credicarro.proposals.model.BiometryStatus;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.DepositAccount;
import twila.credicarro.proposals.model.DepositAccountType;
import twila.credicarro.proposals.model.DocumentFile;
import twila.credicarro.proposals.model.DocumentStatus;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.PendingAction;
import twila.credicarro.proposals.model.PendingActionKind;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalStep;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.proposals.model.StepSituation;
import twila.credicarro.proposals.model.VehicleDetails;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

class ProposalsClientWireMockTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);

    @Test
    void createSendsCpfAndSimulationIdAndReturnsTheProposalId() {
        server.stubFor(post(urlEqualTo(path("v2/proposal"))).willReturn(jsonResponse(201, "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "'}")));

        UUID id = client.proposals().create(CreateProposalRequest.builder().cpf("12345678909").simulationId(UUID.fromString(Fixtures.SIMULATION_ID)).build());

        assertThat(id).isEqualTo(proposalId);
        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal"))).withRequestBody(
                equalToJson(json("{'cpf':'12345678909','simulacaoId':'" + Fixtures.SIMULATION_ID + "'}"))));
    }

    @Test
    void createMapsBusinessRuleErrorsByStableCode() {
        server.stubFor(post(urlEqualTo(path("v2/proposal"))).willReturn(jsonResponse(422,
                "{'tipo':'Proposal.SimulationAlreadyActivated','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.proposals().create(CreateProposalRequest.builder().cpf("12345678909").simulationId(proposalId).build()))
                .isInstanceOf(CredicarroBusinessRuleException.class)
                .satisfies(ex -> assertThat(((CredicarroBusinessRuleException) ex).getErrorCode()).isEqualTo("Proposal.SimulationAlreadyActivated"));
    }

    @Test
    void getMapsTheDetailedSnapshot() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID))).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        ProposalDetails details = client.proposals().get(proposalId);

        assertThat(details.getId()).isEqualTo(proposalId);
        assertThat(details.getNumber()).isEqualTo(171020250001L);
        assertThat(details.getSimulationId()).isEqualTo(UUID.fromString(Fixtures.SIMULATION_ID));
        assertThat(details.getStatus()).isEqualTo(ProposalStatus.APPROVED);
        assertThat(details.getStatusRaw()).isEqualTo(2);
        assertThat(details.getStatusName()).isEqualTo("APROVADA");
        assertThat(details.getType()).isEqualTo(ProposalType.REFINANCING);
        assertThat(details.getCreatedAt().getOffset().getTotalSeconds()).isEqualTo(-3 * 3600);
        assertThat(details.getRefusalReason()).isNull();
        assertThat(details.getBiometryLink()).isEqualTo("https://onboarding.exemplo.com/sessao/abc123");
        assertThat(details.getSeller()).isNull();

        assertThat(details.getBuyer().getDocument()).isEqualTo("12345678909");
        assertThat(details.getBuyer().getBiometry()).isEqualTo(BiometryStatus.APPROVED);
        assertThat(details.getBuyer().getBiometryRaw()).isEqualTo(3);
        assertThat(details.getBuyer().getBiometryName()).isEqualTo("APROVADA");
        assertThat(details.getBuyer().getActiveStep().getStep()).isEqualTo(ProposalStep.DOCUMENT_CORRECTION);
        assertThat(details.getBuyer().getActiveStep().getStepRaw()).isEqualTo(7);
        assertThat(details.getBuyer().getActiveStep().getStepName()).isEqualTo("CORRECAO_DOCUMENTOS");
        assertThat(details.getBuyer().getActiveStep().getSituation()).isEqualTo(StepSituation.PENDING_CORRECTION);
        assertThat(details.getBuyer().getActiveStep().getActionOwner()).isEqualTo(ActionOwner.INTEGRATOR);
        assertThat(details.getBuyer().getCompletedSteps()).hasSize(2);
        assertThat(details.getBuyer().getCompletedSteps().get(1).getStep()).isEqualTo(ProposalStep.BIOMETRY_ANALYSIS);
        assertThat(details.getBuyer().getCompletedSteps().get(1).getDate()).isNotNull();
        assertThat(details.getBuyer().getDocuments()).hasSize(3);
        assertThat(details.getBuyer().getDocuments().get(0).getType()).isEqualTo(DocumentType.CNH);
        assertThat(details.getBuyer().getDocuments().get(0).getStatus()).isEqualTo(DocumentStatus.INVALID);
        assertThat(details.getBuyer().getDocuments().get(0).getReason()).isEqualTo("Documento ilegível");
        assertThat(details.getBuyer().getDocuments().get(1).getReason()).isNull();

        assertThat(details.getStore().getDocument()).isEqualTo("11222333000181");
        assertThat(details.getStore().getBiometry()).isNull();
        assertThat(details.getStore().getActiveStep().getStep()).isEqualTo(ProposalStep.DOCUMENT_SUBMISSION);
    }

    @Test
    void getPendingActionsReflectTheIntegratorOwnedSteps() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID))).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        List<PendingAction> actions = client.proposals().get(proposalId).getPendingActions();

        assertThat(actions).hasSize(2);
        assertThat(actions.get(0).getParticipant()).isEqualTo(ProposalParticipant.BUYER);
        assertThat(actions.get(0).getKind()).isEqualTo(PendingActionKind.CORRECT_DOCUMENTS);
        assertThat(actions.get(0).getDocumentTypes()).containsExactly(DocumentType.CNH);
        assertThat(actions.get(1).getParticipant()).isEqualTo(ProposalParticipant.STORE);
        assertThat(actions.get(1).getKind()).isEqualTo(PendingActionKind.SEND_DOCUMENTS);
        assertThat(actions.get(1).getDocumentTypes()).containsExactly(DocumentType.ECV_REPORT);
    }

    @Test
    void unrecognizedCodesBecomeUnknownButKeepTheRawValues() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID))).willReturn(jsonResponse(200,
                "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "','enum_status':77,'status':'NOVO_STATUS','tipo':'Leasing',"
                        + "'comprador':{'cpf':'1','enum_biometria':9,'biometria':'X',"
                        + "'status_ativo':{'enum_etapa':99,'etapa':'NOVA','enum_situacao':88,'situacao':'S','enum_responsavel_acao':66,'responsavel_acao':'R'},"
                        + "'documentos':[{'enum_tipo':123,'tipo':'OUTRO','enum_status':55,'status':'Z'}]}}")));

        ProposalDetails details = client.proposals().get(proposalId);

        assertThat(details.getStatus()).isEqualTo(ProposalStatus.UNKNOWN);
        assertThat(details.getStatusRaw()).isEqualTo(77);
        assertThat(details.getStatusName()).isEqualTo("NOVO_STATUS");
        assertThat(details.getType()).isEqualTo(ProposalType.UNKNOWN);
        assertThat(details.getBuyer().getBiometry()).isEqualTo(BiometryStatus.UNKNOWN);
        assertThat(details.getBuyer().getBiometryRaw()).isEqualTo(9);
        assertThat(details.getBuyer().getActiveStep().getStep()).isEqualTo(ProposalStep.UNKNOWN);
        assertThat(details.getBuyer().getActiveStep().getStepRaw()).isEqualTo(99);
        assertThat(details.getBuyer().getActiveStep().getSituation()).isEqualTo(StepSituation.UNKNOWN);
        assertThat(details.getBuyer().getActiveStep().getActionOwner()).isEqualTo(ActionOwner.UNKNOWN);
        assertThat(details.getBuyer().getDocuments().get(0).getType()).isEqualTo(DocumentType.UNKNOWN);
        assertThat(details.getBuyer().getDocuments().get(0).getTypeRaw()).isEqualTo(123);
        assertThat(details.getBuyer().getDocuments().get(0).getStatus()).isEqualTo(DocumentStatus.UNKNOWN);
        assertThat(details.getBuyer().getDocuments().get(0).getStatusRaw()).isEqualTo(55);
        assertThat(details.getPendingActions()).isEmpty();
    }

    @Test
    void notFoundSurfacesAsAnApiException() {
        server.stubFor(get(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID))).willReturn(jsonResponse(404,
                "{'tipo':'Proposal.Access.NotFound','titulo':'Não encontrado','status':404}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Proposal.Access.NotFound"));
    }

    @Test
    void listUsesTheV1RouteWithPaginationAndMapsEnglishStatus() {
        String item = "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "','numero':171020250001,'id_simulacao':'" + Fixtures.SIMULATION_ID
                + "','tipo':'Financing','status':'ManualAnalysis','comprador':{'cpf':'123.456.789-01'},'vendedor':{'cpf':'987.654.321-00'},"
                + "'data_criacao':'2025-10-17T19:05:08.984Z'}";
        server.stubFor(get(urlEqualTo(path("v1/proposal?pagina_inicial=1&limite=10")))
                .willReturn(jsonResponse(200, Fixtures.page(item, false, false, 1, 10, 1))));

        PagedResult<ProposalSummary> page = client.proposals().list();

        assertThat(page.getItems()).hasSize(1);
        ProposalSummary summary = page.getItems().get(0);
        assertThat(summary.getId()).isEqualTo(proposalId);
        assertThat(summary.getNumber()).isEqualTo(171020250001L);
        assertThat(summary.getSimulationId()).isEqualTo(UUID.fromString(Fixtures.SIMULATION_ID));
        assertThat(summary.getType()).isEqualTo(ProposalType.FINANCING);
        assertThat(summary.getStatus()).isEqualTo(ProposalStatus.MANUAL_ANALYSIS);
        assertThat(summary.getStatusName()).isEqualTo("ManualAnalysis");
        assertThat(summary.getBuyerCpf()).isEqualTo("123.456.789-01");
        assertThat(summary.getSellerCpf()).isEqualTo("987.654.321-00");
        assertThat(page.getTotalCount()).isEqualTo(1L);
    }

    @Test
    void listSummaryWithoutSellerHasNullSellerCpf() {
        String item = "{'id_proposta':'" + Fixtures.PROPOSAL_ID + "','tipo':'Refinancing','status':'Approved','comprador':{'cpf':'1'}}";
        server.stubFor(get(urlEqualTo(path("v1/proposal?pagina_inicial=3&limite=5")))
                .willReturn(jsonResponse(200, Fixtures.page(item, false, true, 3, 5, 11))));

        PagedResult<ProposalSummary> page = client.proposals().list(ListRequest.of(3, 5));

        assertThat(page.getItems().get(0).getSellerCpf()).isNull();
        assertThat(page.getItems().get(0).getStatus()).isEqualTo(ProposalStatus.APPROVED);
    }

    @Test
    void sendVehicleDetailsPostsTheFourFieldsAndAccepts204() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/vehicle-detail"))).willReturn(aResponse().withStatus(204)));

        client.proposals().sendVehicleDetails(proposalId,
                VehicleDetails.builder().renavam("12345678900").chassis("9BWZZZ377VT004251").licensePlate("ABC1D23").color("PRATA").build());

        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/vehicle-detail"))).withRequestBody(
                equalToJson(json("{'renavam':'12345678900','chassi':'9BWZZZ377VT004251','placa':'ABC1D23','cor':'PRATA'}"))));
    }

    @Test
    void sendDepositAccountPostsTheBankDataWithTheAccountTypeName() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/deposit-account"))).willReturn(aResponse().withStatus(204)));

        client.proposals().sendDepositAccount(proposalId, DepositAccount.builder().holderCpf("12345678909").bankCode("001")
                .branch("1234").branchDigit("5").accountNumber("123456").accountDigit("7").accountType(DepositAccountType.PAYMENT).build());

        server.verify(postRequestedFor(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/deposit-account"))).withRequestBody(
                equalToJson(json("{'cpfTitular':'12345678909','codigoBanco':'001','agencia':'1234','digitoAgencia':'5','conta':'123456',"
                        + "'digitoConta':'7','tipoConta':'PAGAMENTO'}"))));
    }

    @Test
    void depositAccountOnlyForRefinancingSurfacesTheBusinessRule() {
        server.stubFor(post(urlEqualTo(path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/deposit-account"))).willReturn(jsonResponse(422,
                "{'tipo':'Proposal.DepositAccount.OnlyRefinancing','titulo':'Regra de negócio não atendida','status':422}")));

        assertThatThrownBy(() -> client.proposals().sendDepositAccount(proposalId, DepositAccount.builder().accountType(DepositAccountType.CHECKING).build()))
                .isInstanceOf(CredicarroBusinessRuleException.class);
    }

    @Test
    void uploadDocumentPostsTheBase64PayloadWithTheParticipantNameAndTypeCode() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(204)));
        byte[] content = {37, 80, 68, 70, 45};

        client.proposals().uploadDocument(proposalId, ProposalDocumentUpload.builder()
                .participant(ProposalParticipant.STORE)
                .documentType(DocumentType.ECV_REPORT)
                .file(DocumentFile.fromBytes(content, "laudo-ecv.pdf"))
                .build(), "doc-123");

        server.verify(postRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(json(
                "{'participante':'LOJA','tipoDocumento':8,'nomeArquivo':'laudo-ecv.pdf','conteudoBase64':'"
                        + Base64.getEncoder().encodeToString(content) + "'}"))));
    }

    @Test
    void uploadDocumentConflictSurfacesTheIdempotencyKeyConflict() {
        String url = path("v2/proposal/" + Fixtures.PROPOSAL_ID + "/document");
        server.stubFor(post(urlEqualTo(url)).willReturn(jsonResponse(409, "{'tipo':'Idempotency.KeyConflict','titulo':'Conflito','status':409}")));

        assertThatThrownBy(() -> client.proposals().uploadDocument(proposalId, ProposalDocumentUpload.builder()
                .participant(ProposalParticipant.BUYER).documentType(DocumentType.CNH)
                .file(DocumentFile.fromBytes(new byte[] {1}, "cnh.pdf")).build()))
                .isInstanceOf(CredicarroApiException.class)
                .satisfies(ex -> assertThat(((CredicarroApiException) ex).getErrorCode()).isEqualTo("Idempotency.KeyConflict"));

        server.verify(1, postRequestedFor(urlEqualTo(url)));
    }
}
