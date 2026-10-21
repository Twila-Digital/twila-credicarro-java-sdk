package twila.credicarro.internal.http;

import java.util.UUID;

/**
 * Rotas da API (relativas à base {@code .../integration/}). Os modelos usam {@code %s} no lugar dos parâmetros de
 * caminho; os contract tests comparam estas constantes com o {@code openapi.yaml}.
 */
public final class Routes {

    public static final String ACCESS_TOKEN = "v1/authentication/accesstoken";

    public static final String CUSTOMER = "v1/customer";
    public static final String CUSTOMER_UNIFIED = "v1/customer/unified";
    public static final String CUSTOMER_ADDRESS = "v1/customer/address";
    public static final String CUSTOMER_PROFESSIONAL = "v1/customer/professional";

    public static final String V1_RANGE_FINANCING = "v1/proposal/simulation/range/financing";
    public static final String V1_RANGE_REFINANCING = "v1/proposal/simulation/range/refinancing";
    public static final String V1_SIMULATION_FINANCING = "v1/proposal/simulation/financing";
    public static final String V1_SIMULATION_REFINANCING = "v1/proposal/simulation/refinancing";
    public static final String V1_SIMULATIONS = "v1/proposal/simulation";
    public static final String V1_SIMULATION_BY_ID = "v1/proposal/simulation/%s";
    public static final String V1_PROPOSALS = "v1/proposal";
    public static final String V1_PROPOSAL_BY_ID = "v1/proposal/%s";

    public static final String V2_RANGE_FINANCING = "v2/proposal/simulation/range/financing";
    public static final String V2_RANGE_REFINANCING = "v2/proposal/simulation/range/refinancing";
    public static final String V2_SIMULATION_FINANCING = "v2/proposal/simulation/financing";
    public static final String V2_SIMULATION_REFINANCING = "v2/proposal/simulation/refinancing";
    public static final String V2_PROPOSALS = "v2/proposal";
    public static final String V2_PROPOSAL_BY_ID = "v2/proposal/%s";
    public static final String V2_PROPOSAL_VEHICLE_DETAIL = "v2/proposal/%s/vehicle-detail";
    public static final String V2_PROPOSAL_DEPOSIT_ACCOUNT = "v2/proposal/%s/deposit-account";
    public static final String V2_PROPOSAL_DOCUMENT = "v2/proposal/%s/document";

    public static final String WEBHOOKS = "v1/webhook";
    public static final String WEBHOOK_BY_ID = "v1/webhook/%s";

    private Routes() {
    }

    public static String format(String template, UUID id) {
        return String.format(template, id);
    }
}
