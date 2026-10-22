package twila.credicarro.support;

/**
 * Payloads de exemplo (derivados do openapi.yaml e das páginas da documentação). Aspas simples viram aspas duplas.
 */
public final class Fixtures {

    public static final String PROPOSAL_ID = "bea8d060-f93d-48eb-b6c4-45f64483cdd9";
    public static final String SIMULATION_ID = "123e4567-e89b-12d3-a456-426614174000";
    public static final String WEBHOOK_ID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";
    public static final String CUSTOMER_ID = "0b6f4a3e-8b7a-4c55-9d11-2f0c4b8f2a10";

    /**
     * Corpo do vetor de assinatura da documentação (webhooks/security).
     */
    public static final String SIGNATURE_VECTOR_BODY =
            "{\"id_proposta\":\"bea8d060-f93d-48eb-b6c4-45f64483cdd9\",\"enum_status\":2,\"status\":\"Approved\"}";

    private Fixtures() {
    }

    public static String json(String value) {
        return value.replace('\'', '"');
    }

    private static final String ACTIVE_DOCUMENT_CORRECTION = "{'enum_etapa':7,'etapa':'CORRECAO_DOCUMENTOS','enum_situacao':4,"
            + "'situacao':'PENDENTE_CORRECAO','enum_responsavel_acao':2,'responsavel_acao':'INTEGRADOR'}";

    private static final String ACTIVE_DOCUMENT_SUBMISSION = "{'enum_etapa':6,'etapa':'ENVIO_DOCUMENTOS','enum_situacao':1,"
            + "'situacao':'AGUARDANDO_ENVIO','enum_responsavel_acao':2,'responsavel_acao':'INTEGRADOR'}";

    /**
     * Resposta de {@code GET v2/proposal/{id}}: comprador em correção de documentos, loja aguardando documentos, sem vendedor.
     */
    public static String proposalV2() {
        return json("{"
                + "'id_proposta':'" + PROPOSAL_ID + "',"
                + "'numero':171020250001,"
                + "'id_simulacao':'" + SIMULATION_ID + "',"
                + "'enum_status':2,"
                + "'status':'APROVADA',"
                + "'tipo':'Refinancing',"
                + "'data_criacao':'2026-10-12T14:05:08.984-03:00',"
                + "'motivo_recusa':null,"
                + "'link_biometria':'https://onboarding.exemplo.com/sessao/abc123',"
                + "'comprador':{"
                + "'cpf':'12345678909','enum_biometria':3,'biometria':'APROVADA',"
                + "'status_ativo':" + ACTIVE_DOCUMENT_CORRECTION + ","
                + "'etapas_concluidas':["
                + "{'enum_etapa':1,'etapa':'PROPOSTA_CRIADA','data':'2026-10-12T14:05:08-03:00'},"
                + "{'enum_etapa':4,'etapa':'ANALISE_BIOMETRIA','data':'2026-10-12T14:20:00-03:00'}],"
                + "'documentos':["
                + "{'enum_tipo':1,'tipo':'CNH','enum_status':2,'status':'INVALIDO','motivo':'Documento ilegível'},"
                + "{'enum_tipo':5,'tipo':'COMPROVANTE_RESIDENCIA','enum_status':1,'status':'VALIDO'},"
                + "{'enum_tipo':4,'tipo':'COMPROVANTE_RENDA','enum_status':5,'status':'PENDENTE'}]},"
                + "'loja':{"
                + "'cnpj':'11222333000181',"
                + "'status_ativo':" + ACTIVE_DOCUMENT_SUBMISSION + ","
                + "'etapas_concluidas':[],"
                + "'documentos':["
                + "{'enum_tipo':8,'tipo':'LAUDO_ECV','enum_status':5,'status':'PENDENTE'},"
                + "{'enum_tipo':6,'tipo':'DOCUMENTO_VEICULO','enum_status':3,'status':'EM_ANALISE'}]}"
                + "}");
    }

    /**
     * Payload do webhook v2 (v2/webhooks.mdx).
     */
    public static String webhookV2() {
        return json("{"
                + "'id_proposta':'" + PROPOSAL_ID + "',"
                + "'enum_status':2,"
                + "'status':'APROVADA',"
                + "'motivo_evento':'DOCUMENTOS_ALTERADOS',"
                + "'motivo_recusa':null,"
                + "'data_evento':'2026-10-12T17:20:31.512+00:00',"
                + "'comprador':{"
                + "'cpf':'12345678909','enum_biometria':3,'biometria':'APROVADA',"
                + "'status_ativo':" + ACTIVE_DOCUMENT_CORRECTION + ","
                + "'etapas_concluidas':[{'enum_etapa':1,'etapa':'PROPOSTA_CRIADA','data':'2026-10-12T14:05:08-03:00'}],"
                + "'documentos':[{'enum_tipo':1,'tipo':'CNH','enum_status':2,'status':'INVALIDO','motivo':'Documento ilegível'}]},"
                + "'loja':{'cnpj':'11222333000181','status_ativo':" + ACTIVE_DOCUMENT_SUBMISSION + ",'etapas_concluidas':[],'documentos':[]},"
                + "'vendedor':null}");
    }

    /**
     * Item de simulação (SimplifySimulationListResponse).
     */
    public static String simulationOption(String id, int term) {
        return "{'id_simulacao':'" + id + "','valor_solicitado':40000,'prazo':" + term + ",'valor_parcela':2042.42,"
                + "'valor_tac':1500,'valor_boleto':3.5,'valor_seguro':0,'valor_iof':872,'percentual_iof':1.92,"
                + "'valor_credito_total':42722,'valor_divida_total':73527.12,'percentual_cet_mensal':3.62,"
                + "'percentual_cet_anual':53.25,'percentual_juros_mensal':3.29,'percentual_juros_anual':47.53,"
                + "'valor_retencao_diaria_aproximado':12.5}";
    }

    /**
     * Página (schema Page) com os itens informados.
     */
    public static String page(String items, boolean hasNext, boolean hasPrevious, int number, int size, long total) {
        return json("{'itens':[" + items + "],'pagina':{'tem_proximo':" + hasNext + ",'tem_anterior':" + hasPrevious
                + ",'numero':" + number + ",'tamanho':" + size + ",'total':" + total + "}}");
    }
}
