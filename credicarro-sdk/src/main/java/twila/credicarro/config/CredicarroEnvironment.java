package twila.credicarro.config;

import java.net.URI;

/**
 * Ambiente da API do Credicarro a ser utilizado pelo client.
 */
public enum CredicarroEnvironment {

    /**
     * Ambiente de staging ({@code https://api.staging.credicarro.com.br/integration}).
     */
    STAGING("https://api.staging.credicarro.com.br/integration/"),

    /**
     * Ambiente de produção ({@code https://api.credicarro.com.br/integration}).
     */
    PRODUCTION("https://api.credicarro.com.br/integration/");

    private final URI baseUri;

    CredicarroEnvironment(String baseUrl) {
        this.baseUri = URI.create(baseUrl);
    }

    public URI toBaseUri() {
        return baseUri;
    }
}
