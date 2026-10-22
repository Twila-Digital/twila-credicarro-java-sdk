package twila.credicarro.proposals.model;

import java.util.UUID;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Criação de proposta a partir de uma opção de simulação ({@code simulacaoId}).
 */
@Value
@Builder
public class CreateProposalRequest {
    String cpf;
    UUID simulationId;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateProposalRequest", "cpf", Masking.cpf(cpf), "simulationId", simulationId);
    }
}
