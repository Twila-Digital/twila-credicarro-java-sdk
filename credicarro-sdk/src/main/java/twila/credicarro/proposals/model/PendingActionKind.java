package twila.credicarro.proposals.model;

/**
 * Ação que o integrador precisa executar (ver {@link ProposalDetails#getPendingActions()}).
 */
public enum PendingActionKind {

    /**
     * Etapa {@code ENVIO_DOCUMENTOS} (6): enviar os documentos pendentes.
     */
    SEND_DOCUMENTS,

    /**
     * Etapa {@code CORRECAO_DOCUMENTOS} (7): reenviar os documentos recusados (com nova chave de idempotência).
     */
    CORRECT_DOCUMENTS,

    /**
     * Etapa {@code DADOS_VEICULO} (8): enviar os dados do veículo.
     */
    SEND_VEHICLE_DETAILS,

    /**
     * Etapa {@code CONTA_RECEBIMENTO} (5): enviar a conta de recebimento (refinanciamento).
     */
    SEND_DEPOSIT_ACCOUNT
}
