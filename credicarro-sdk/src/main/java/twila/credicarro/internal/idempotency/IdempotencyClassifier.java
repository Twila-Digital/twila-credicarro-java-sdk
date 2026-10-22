package twila.credicarro.internal.idempotency;

import java.util.regex.Pattern;

/**
 * Decide, por método e rota, quando o SDK gera {@code Idempotency-Key} automaticamente e quando a requisição é
 * segura de repetir (retry).
 *
 * <p>Regras (ver documentação "Erros e idempotência"):
 * <ul>
 *   <li>Chave automática: cadastro de cliente, endereço, dados profissionais, simulações e propostas (v1/v2) e envio
 *       de documento. <b>Nunca</b> em {@code POST v1/customer/unified}: com a chave a rota só cria (CPF existente
 *       retorna {@code 409 Cpf.CpfAlreadyAdded}); sem a chave ela faz upsert.</li>
 *   <li>Seguras de repetir: {@code GET}; token; {@code POST} com chave; dados do veículo e conta de recebimento
 *       (semântica de substituição); {@code PUT}/{@code DELETE} de webhook.</li>
 *   <li>Não seguras: {@code POST v1/webhook} e {@code POST v1/customer/unified} sem chave.</li>
 * </ul>
 */
public final class IdempotencyClassifier {

    private static final Pattern AUTOMATIC_KEY_ROUTES = Pattern.compile(
            "^(v1/customer(/address|/professional)?"
                    + "|v[12]/proposal/simulation/(financing|refinancing)"
                    + "|v[12]/proposal"
                    + "|v2/proposal/[^/]+/document)$");

    private static final Pattern UNIFIED_ROUTE = Pattern.compile("^v1/customer/unified$");

    private static final Pattern REPLACEMENT_ROUTES = Pattern.compile("^v2/proposal/[^/]+/(vehicle-detail|deposit-account)$");

    private static final Pattern ACCESS_TOKEN_ROUTE = Pattern.compile("^v1/authentication/accesstoken$");

    private static final Pattern WEBHOOK_BY_ID_ROUTE = Pattern.compile("^v1/webhook/[^/]+$");

    private IdempotencyClassifier() {
    }

    /**
     * Indica se o SDK gera uma {@code Idempotency-Key} automática (a mesma em todas as tentativas) para a rota.
     */
    public static boolean requiresAutomaticKey(String method, String path) {
        return "POST".equalsIgnoreCase(method) && AUTOMATIC_KEY_ROUTES.matcher(routeOf(path)).matches();
    }

    /**
     * Indica se a rota aceita o cabeçalho {@code Idempotency-Key} (inclui {@code v1/customer/unified}, onde ele é só explícito).
     */
    public static boolean acceptsIdempotencyKey(String method, String path) {
        return "POST".equalsIgnoreCase(method)
                && (AUTOMATIC_KEY_ROUTES.matcher(routeOf(path)).matches() || UNIFIED_ROUTE.matcher(routeOf(path)).matches());
    }

    public static boolean isRetrySafe(String method, String path, boolean hasIdempotencyKey) {
        String route = routeOf(path);

        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method))
            return true;

        if ("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
            return WEBHOOK_BY_ID_ROUTE.matcher(route).matches();

        if (!"POST".equalsIgnoreCase(method))
            return false;

        if (ACCESS_TOKEN_ROUTE.matcher(route).matches() || REPLACEMENT_ROUTES.matcher(route).matches())
            return true;

        return hasIdempotencyKey && acceptsIdempotencyKey(method, path);
    }

    /**
     * Valida uma chave explícita: 1 a 200 caracteres ASCII imprimíveis (0x20 a 0x7E), não só espaços. Chave vazia nunca
     * equivale a "sem chave".
     */
    public static void validateKey(String idempotencyKey) {
        boolean valid = idempotencyKey != null
                && idempotencyKey.length() >= 1
                && idempotencyKey.length() <= 200
                && !idempotencyKey.trim().isEmpty();

        for (int i = 0; valid && i < idempotencyKey.length(); i++) {
            char c = idempotencyKey.charAt(i);
            valid = c >= 0x20 && c <= 0x7E;
        }

        if (!valid)
            throw new IllegalArgumentException("idempotencyKey deve ter de 1 a 200 caracteres ASCII imprimíveis.");
    }

    private static String routeOf(String path) {
        String route = path;

        int query = route.indexOf('?');
        if (query >= 0)
            route = route.substring(0, query);

        return route.startsWith("/") ? route.substring(1) : route;
    }
}
