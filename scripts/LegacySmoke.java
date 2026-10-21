import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import twila.credicarro.CredicarroClient;
import twila.credicarro.ListRequest;
import twila.credicarro.PagedResult;
import twila.credicarro.errors.CredicarroBusinessRuleException;
import twila.credicarro.errors.CredicarroConfigurationException;
import twila.credicarro.proposals.model.ProposalSummary;
import twila.credicarro.proposals.model.ProposalType;

/**
 * Smoke test do SDK no JDK 8, sem framework de teste (o WireMock 3 dos testes unitários exige JDK 11+).
 * Sobe um servidor HTTP do próprio JDK e exercita o caminho real: token, GET v1/proposal, GET v1/proposal/{id} e erro 422.
 */
public final class LegacySmoke {
    private static final String PROPOSAL_ID = "41bcdbdc-413a-43e7-a057-9a89e6d8b775";

    private LegacySmoke() {
    }

    public static void main(String[] args) throws IOException {
        System.out.println("java.version=" + System.getProperty("java.version"));
        expectConfigurationError();

        AtomicInteger tokenCalls = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/integration/v1/authentication/accesstoken", exchange -> {
            tokenCalls.incrementAndGet();
            respond(exchange, 200, "{\"token_de_acesso\":\"token-legacy\",\"expira_em_segundos\":3600,"
                    + "\"tipo_de_token\":\"Bearer\",\"escopo\":\"integration/all\"}");
        });
        server.createContext("/integration/v1/proposal", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (!"Bearer token-legacy".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                respond(exchange, 401, "");
            } else if (path.endsWith("/v1/proposal")) {
                respond(exchange, 200, "{\"itens\":[" + proposalJson() + "],"
                        + "\"pagina\":{\"numero\":1,\"tamanho\":5,\"total\":1,\"tem_proximo\":false,\"tem_anterior\":false}}");
            } else if (path.endsWith(PROPOSAL_ID)) {
                respond(exchange, 200, proposalJson());
            } else {
                respond(exchange, 422, "{\"tipo\":\"Proposal.Access.NotFound\",\"titulo\":\"Regra de negócio não atendida\","
                        + "\"status\":422}");
            }
        });
        server.start();

        URI baseUrl = URI.create("http://localhost:" + server.getAddress().getPort() + "/integration/");
        try (CredicarroClient client = CredicarroClient.builder()
                .clientId("client-id")
                .clientSecret("client-secret")
                .baseUrl(baseUrl)
                .build()) {
            PagedResult<ProposalSummary> page = client.proposals().list(ListRequest.of(1, 5));
            check(page.getTotalCount() == 1 && page.getItems().size() == 1, "listagem com 1 item");
            ProposalSummary first = page.getItems().get(0);
            check(first.getType() == ProposalType.FINANCING && "Expired".equals(first.getStatusName()), "tipo e status da listagem");

            ProposalSummary proposal = client.v1().proposals().get(UUID.fromString(PROPOSAL_ID));
            check(proposal.getNumber() == 202608188L, "número da proposta");

            try {
                client.v1().proposals().get(UUID.randomUUID());
                throw new AssertionError("esperava CredicarroBusinessRuleException no 422");
            } catch (CredicarroBusinessRuleException expected) {
                check(expected.getStatusCode() == 422, "422 vira CredicarroBusinessRuleException");
            }
            check(tokenCalls.get() == 1, "token gerado uma vez e reaproveitado");
            System.out.printf("OK — total=%d numero=%d tipo=%s status=%s chamadasDeToken=%d%n",
                    page.getTotalCount(), proposal.getNumber(), proposal.getType(), proposal.getStatusName(), tokenCalls.get());
        } finally {
            server.stop(0);
        }
        System.out.println("RESULTADO: SUCESSO");
    }

    private static String proposalJson() {
        return "{\"id_proposta\":\"" + PROPOSAL_ID + "\",\"numero\":202608188,"
                + "\"id_simulacao\":\"7877af0c-bfbf-4d48-8b7c-f896daf5b9f6\",\"tipo\":\"Financing\",\"status\":\"Expired\","
                + "\"comprador\":{\"cpf\":\"77114299087\"},\"data_criacao\":\"2026-08-18T18:00:43.8175715+00:00\"}";
    }

    private static void expectConfigurationError() {
        try {
            CredicarroClient.builder().clientId("").clientSecret("x").baseUrl(URI.create("https://localhost/")).build();
            throw new AssertionError("esperava CredicarroConfigurationException com clientId vazio");
        } catch (CredicarroConfigurationException expected) {
            System.out.println("OK — configuração inválida lança CredicarroConfigurationException");
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", status == 422 ? "application/problem+json" : "application/json");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
        exchange.close();
    }

    private static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("falhou: " + what);
        }
    }
}
