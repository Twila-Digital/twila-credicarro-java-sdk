package twila.credicarro.proposals.model;

import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Documento de um participante na consulta/webhook, com status e (quando recusado) o motivo.
 */
@Value
public class ProposalDocumentState {
    DocumentType type;
    int typeRaw;
    String typeName;
    DocumentStatus status;
    int statusRaw;
    String statusName;
    String reason;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalDocumentState", "type", type, "typeRaw", typeRaw, "typeName", typeName, "status", status, "statusRaw", statusRaw, "statusName", statusName, "reason", Masking.mask(reason));
    }
}
