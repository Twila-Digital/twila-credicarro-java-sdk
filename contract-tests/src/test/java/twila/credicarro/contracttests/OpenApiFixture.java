package twila.credicarro.contracttests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Baixa o {@code openapi.yaml} público da documentação (o swagger de staging exige autenticação).
 * Para apontar para outra cópia, defina {@code CREDICARRO_CONTRACT_TESTS_OPENAPI_URL}.
 */
final class OpenApiFixture {

    static final String DEFAULT_URL = "https://documentacao.credicarro.com.br/openapi.yaml";
    static final String URL_VARIABLE = "CREDICARRO_CONTRACT_TESTS_OPENAPI_URL";

    private static JsonNode cached;

    private OpenApiFixture() {
    }

    static synchronized JsonNode fetch() {
        if (cached != null)
            return cached;

        String configured = System.getenv(URL_VARIABLE);
        URI uri = URI.create(configured != null && !configured.trim().isEmpty() ? configured.trim() : DEFAULT_URL);

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new IllegalStateException("Falha ao buscar o openapi.yaml (" + uri + "): HTTP " + response.statusCode());

            cached = new YAMLMapper().readTree(response.body());
            return cached;
        } catch (IOException ex) {
            throw new IllegalStateException("Falha de rede ou de leitura ao buscar o openapi.yaml (" + uri + ").", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Busca do openapi.yaml interrompida.", ex);
        }
    }

    /**
     * Segue {@code $ref} locais ({@code #/components/...}) até o nó real.
     */
    static JsonNode resolve(JsonNode root, JsonNode node) {
        JsonNode current = node;

        for (int depth = 0; depth < 20 && current != null && current.has("$ref"); depth++) {
            String ref = current.get("$ref").asText();
            if (!ref.startsWith("#/"))
                throw new IllegalStateException("Referência não suportada: " + ref);

            current = root.at(ref.substring(1));
            if (current.isMissingNode())
                throw new IllegalStateException("Referência não encontrada no openapi.yaml: " + ref);
        }

        return current;
    }
}
