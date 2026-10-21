package twila.credicarro.proposals.model;

import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Envio de um documento de participante (um arquivo por chamada).
 */
@Value
@Builder
public class ProposalDocumentUpload {
    ProposalParticipant participant;
    DocumentType documentType;
    DocumentFile file;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalDocumentUpload", "participant", participant, "documentType", documentType, "file", file);
    }
}
