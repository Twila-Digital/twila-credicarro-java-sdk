package twila.credicarro.proposals.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Proposta da listagem e da consulta v1 (schema ProposalResponse). Para o retrato completo use {@link ProposalDetails}.
 */
@Value
public class ProposalSummary {
    UUID id;
    Long number;
    UUID simulationId;
    ProposalType type;
    ProposalStatus status;
    String statusName;
    String buyerCpf;
    String sellerCpf;
    OffsetDateTime createdAt;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProposalSummary", "id", id, "number", number, "simulationId", simulationId, "type", type, "status", status, "statusName", statusName, "buyerCpf", Masking.cpf(buyerCpf), "sellerCpf", Masking.cpf(sellerCpf), "createdAt", createdAt);
    }
}
