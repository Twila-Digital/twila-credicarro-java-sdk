package twila.credicarro.proposals;

import java.util.UUID;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.proposals.model.CreateProposalRequest;
import twila.credicarro.proposals.model.DepositAccount;
import twila.credicarro.proposals.model.ProposalDetails;
import twila.credicarro.proposals.model.ProposalDocumentUpload;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.VehicleDetails;

/**
 * Propostas (rotas v2) e a listagem v1. As rotas de criação/consulta v1 ficam em {@code client.v1().proposals()}.
 */
public interface ProposalsClient {

    /**
     * Cria a proposta a partir de uma opção de simulação feita com {@code cnpjLoja} ({@code POST v2/proposal}).
     * O cliente precisa estar com o cadastro completo (endereço, ocupação e renda).
     */
    UUID create(CreateProposalRequest request);

    /**
     * Retrato atual da proposta ({@code GET v2/proposal/:id}): status, biometria, etapas e documentos.
     */
    ProposalDetails get(UUID proposalId);

    default PagedResult<ProposalSummary> list() {
        return list(ListRequest.defaults());
    }

    /**
     * Lista propostas de forma paginada ({@code GET v1/proposal}).
     */
    PagedResult<ProposalSummary> list(ListRequest request);

    /**
     * Envia os dados do veículo ({@code POST v2/proposal/:id/vehicle-detail}, {@code 204}). Aceito enquanto a etapa
     * {@code DADOS_VEICULO} (8) estiver ativa; enviar de novo substitui os dados (por isso pode ser repetido com segurança).
     */
    void sendVehicleDetails(UUID proposalId, VehicleDetails details);

    /**
     * Envia a conta de recebimento de uma proposta de refinanciamento ({@code POST v2/proposal/:id/deposit-account},
     * {@code 204}). Enviar de novo substitui a conta anterior.
     */
    void sendDepositAccount(UUID proposalId, DepositAccount account);

    /**
     * Envia um documento ({@code POST v2/proposal/:id/document}, {@code 204}), um arquivo por chamada. Gera uma
     * {@code Idempotency-Key} para a chamada, a mesma em todas as tentativas automáticas. Prefira
     * {@link #uploadDocument(UUID, ProposalDocumentUpload, String)} e persista a chave por envio.
     */
    void uploadDocument(UUID proposalId, ProposalDocumentUpload upload);

    /**
     * Igual a {@link #uploadDocument(UUID, ProposalDocumentUpload)}, com a {@code Idempotency-Key} informada
     * (1 a 200 caracteres). Recomenda-se persistir uma chave por envio: com a mesma chave e o mesmo conteúdo, um envio
     * bem-sucedido nas últimas 24 horas não é processado de novo. Na correção de um documento recusado use uma
     * <b>nova</b> chave.
     */
    void uploadDocument(UUID proposalId, ProposalDocumentUpload upload, String idempotencyKey);
}
