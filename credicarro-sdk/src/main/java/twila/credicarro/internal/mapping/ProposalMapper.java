package twila.credicarro.internal.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import twila.credicarro.internal.generated.proposal.CreateProposalRequestWire;
import twila.credicarro.internal.generated.proposal.DepositAccountRequestWire;
import twila.credicarro.internal.generated.proposal.DocumentUploadRequestWire;
import twila.credicarro.internal.generated.proposal.ProposalV2ActiveStepWire;
import twila.credicarro.internal.generated.proposal.ProposalV2CompletedStepWire;
import twila.credicarro.internal.generated.proposal.ProposalV2DocumentWire;
import twila.credicarro.internal.generated.proposal.ProposalV2ParticipantWire;
import twila.credicarro.internal.generated.proposal.ProposalV2StoreWire;
import twila.credicarro.internal.generated.proposal.ProposalV2Wire;
import twila.credicarro.internal.generated.proposal.ProposalWire;
import twila.credicarro.internal.generated.proposal.VehicleDetailRequestWire;
import twila.credicarro.proposals.model.ActionOwner;
import twila.credicarro.proposals.model.BiometryStatus;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.DepositAccount;
import twila.credicarro.proposals.model.DocumentStatus;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalActiveStep;
import twila.credicarro.proposals.model.ProposalCompletedStep;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalDocumentState;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalParticipantState;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalStep;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.proposals.model.StepSituation;
import twila.credicarro.proposals.model.VehicleDetails;
import twila.credicarro.serialization.EnumMapping;

public final class ProposalMapper {

    /**
     * Valor bruto usado quando a API não envia o código numérico.
     */
    private static final int ABSENT_RAW = -1;

    private ProposalMapper() {
    }

    public static CreateProposalRequestWire toWire(CreateProposalRequest request) {
        CreateProposalRequestWire wire = new CreateProposalRequestWire();
        wire.cpf = request.getCpf();
        wire.simulacaoId = request.getSimulationId();
        return wire;
    }

    public static VehicleDetailRequestWire toWire(VehicleDetails details) {
        VehicleDetailRequestWire wire = new VehicleDetailRequestWire();
        wire.renavam = details.getRenavam();
        wire.chassi = details.getChassis();
        wire.placa = details.getLicensePlate();
        wire.cor = details.getColor();
        return wire;
    }

    public static DepositAccountRequestWire toWire(DepositAccount account) {
        DepositAccountRequestWire wire = new DepositAccountRequestWire();
        wire.cpfTitular = account.getHolderCpf();
        wire.codigoBanco = account.getBankCode();
        wire.agencia = account.getBranch();
        wire.digitoAgencia = account.getBranchDigit();
        wire.conta = account.getAccountNumber();
        wire.digitoConta = account.getAccountDigit();
        wire.tipoConta = account.getAccountType().wireName();
        return wire;
    }

    public static DocumentUploadRequestWire toWire(ProposalDocumentUpload upload) {
        DocumentUploadRequestWire wire = new DocumentUploadRequestWire();
        wire.participante = upload.getParticipant().wireName();
        wire.tipoDocumento = upload.getDocumentType().wireValue();
        wire.nomeArquivo = upload.getFile().getFileName();
        wire.conteudoBase64 = upload.getFile().getBase64Content();
        return wire;
    }

    public static ProposalDetails toPublic(ProposalV2Wire wire) {
        ProposalStatus status = statusOf(wire.enumStatus, wire.status);

        return new ProposalDetails(
                wire.idProposta,
                wire.numero,
                wire.idSimulacao,
                status,
                wire.enumStatus != null ? wire.enumStatus : status.wireValue(),
                wire.status,
                EnumMapping.fromWireName(ProposalType.class, wire.tipo),
                wire.dataCriacao,
                wire.motivoRecusa,
                wire.linkBiometria,
                toPublic(wire.comprador),
                toPublic(wire.vendedor),
                toPublic(wire.loja));
    }

    public static ProposalSummary toPublic(ProposalWire wire) {
        return new ProposalSummary(
                wire.idProposta,
                wire.numero,
                wire.idSimulacao,
                EnumMapping.fromWireName(ProposalType.class, wire.tipo),
                ProposalStatus.fromName(wire.status),
                wire.status,
                wire.comprador != null ? wire.comprador.cpf : null,
                wire.vendedor != null ? wire.vendedor.cpf : null,
                wire.dataCriacao);
    }

    /**
     * Status a partir do código numérico (preferido) ou, na falta dele, do nome em português ou inglês.
     */
    public static ProposalStatus statusOf(Integer code, String name) {
        return code != null ? ProposalStatus.fromWireValue(code) : ProposalStatus.fromName(name);
    }

    public static ProposalParticipantState toPublic(ProposalV2ParticipantWire wire) {
        if (wire == null)
            return null;

        boolean hasBiometry = wire.enumBiometria != null || wire.biometria != null;
        BiometryStatus biometry = null;
        if (hasBiometry)
            biometry = BiometryStatus.fromWireValue(wire.enumBiometria != null ? wire.enumBiometria : ABSENT_RAW);

        return new ProposalParticipantState(
                wire.cpf,
                biometry,
                wire.enumBiometria,
                wire.biometria,
                toPublic(wire.statusAtivo),
                completedStepsOf(wire.etapasConcluidas),
                documentsOf(wire.documentos));
    }

    public static ProposalParticipantState toPublic(ProposalV2StoreWire wire) {
        if (wire == null)
            return null;

        return new ProposalParticipantState(
                wire.cnpj,
                null,
                null,
                null,
                toPublic(wire.statusAtivo),
                completedStepsOf(wire.etapasConcluidas),
                documentsOf(wire.documentos));
    }

    private static ProposalActiveStep toPublic(ProposalV2ActiveStepWire wire) {
        if (wire == null)
            return null;

        int stepRaw = rawOf(wire.enumEtapa);
        int situationRaw = rawOf(wire.enumSituacao);
        int ownerRaw = rawOf(wire.enumResponsavelAcao);

        return new ProposalActiveStep(
                ProposalStep.fromWireValue(stepRaw), stepRaw, wire.etapa,
                StepSituation.fromWireValue(situationRaw), situationRaw, wire.situacao,
                ActionOwner.fromWireValue(ownerRaw), ownerRaw, wire.responsavelAcao);
    }

    private static List<ProposalCompletedStep> completedStepsOf(List<ProposalV2CompletedStepWire> wires) {
        List<ProposalCompletedStep> steps = new ArrayList<>();

        if (wires != null) {
            for (ProposalV2CompletedStepWire wire : wires) {
                int raw = rawOf(wire.enumEtapa);
                steps.add(new ProposalCompletedStep(ProposalStep.fromWireValue(raw), raw, wire.etapa, wire.data));
            }
        }

        return Collections.unmodifiableList(steps);
    }

    private static List<ProposalDocumentState> documentsOf(List<ProposalV2DocumentWire> wires) {
        List<ProposalDocumentState> documents = new ArrayList<>();

        if (wires != null) {
            for (ProposalV2DocumentWire wire : wires) {
                int typeRaw = rawOf(wire.enumTipo);
                int statusRaw = rawOf(wire.enumStatus);
                documents.add(new ProposalDocumentState(
                        DocumentType.fromWireValue(typeRaw), typeRaw, wire.tipo,
                        DocumentStatus.fromWireValue(statusRaw), statusRaw, wire.status,
                        wire.motivo));
            }
        }

        return Collections.unmodifiableList(documents);
    }

    private static int rawOf(Integer value) {
        return value != null ? value : ABSENT_RAW;
    }
}
