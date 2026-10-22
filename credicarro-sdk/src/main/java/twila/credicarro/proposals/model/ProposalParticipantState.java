package twila.credicarro.proposals.model;

import java.util.List;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Retrato de um participante (comprador, vendedor ou loja). {@code document} é o CPF (comprador/vendedor) ou o
 * CNPJ (loja). {@code biometry} só vem para comprador/vendedor.
 */
@Value
public class ProposalParticipantState {
    String document;
    BiometryStatus biometry;
    Integer biometryRaw;
    String biometryName;
    ProposalActiveStep activeStep;
    List<ProposalCompletedStep> completedSteps;
    List<ProposalDocumentState> documents;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalParticipantState", "document", Masking.cpf(document), "biometry", biometry, "biometryRaw", biometryRaw, "biometryName", biometryName, "activeStep", activeStep, "completedSteps", completedSteps, "documents", documents);
    }
}
