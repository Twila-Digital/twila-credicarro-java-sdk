package twila.credicarro;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import twila.credicarro.config.CredicarroEnvironment;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.customers.CustomersClient;
import twila.credicarro.errors.CredicarroConfigurationException;
import twila.credicarro.proposals.ProposalsClient;
import twila.credicarro.simulations.SimulationsClient;
import twila.credicarro.v1.V1Client;
import twila.credicarro.webhooks.WebhooksClient;

/**
 * Ponto de entrada do SDK. Thread-safe: crie uma instância única (singleton) e reutilize — ela mantém o pool de
 * conexões HTTP, o cache do token de acesso e o estado do circuit breaker. Feche-a só no shutdown da aplicação.
 */
public interface CredicarroClient extends AutoCloseable {

    CustomersClient customers();

    SimulationsClient simulations();

    ProposalsClient proposals();

    WebhooksClient webhooks();

    /**
     * Rotas v1 sem {@code cnpjLoja}.
     */
    V1Client v1();

    @Override
    void close();

    static Builder builder() {
        return new Builder();
    }

    final class Builder {

        private String clientId;
        private String clientSecret;
        private CredicarroEnvironment environment = CredicarroEnvironment.PRODUCTION;
        private URI baseUrl;
        private CredicarroResilienceOptions resilience = CredicarroResilienceOptions.builder().build();

        private Builder() {
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Segredo do cliente. Nunca é incluído em log, {@code toString} ou mensagem de erro do SDK.
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Ambiente da API (padrão: {@link CredicarroEnvironment#PRODUCTION}).
         */
        public Builder environment(CredicarroEnvironment environment) {
            this.environment = environment;
            return this;
        }

        /**
         * Sobrescreve a URL base (ex.: testes). Precisa ser HTTPS, exceto {@code localhost} e {@code 127.0.0.1}.
         */
        public Builder baseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder resilience(CredicarroResilienceOptions resilience) {
            this.resilience = resilience;
            return this;
        }

        public CredicarroClient build() {
            validate();

            URI resolvedBaseUrl = resolveBaseUrl();
            return new CredicarroClientImpl(clientId, clientSecret, resolvedBaseUrl, resilience);
        }

        private URI resolveBaseUrl() {
            URI uri = baseUrl != null ? baseUrl : environment.toBaseUri();
            return uri.toString().endsWith("/") ? uri : URI.create(uri + "/");
        }

        private static boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }

        private static boolean isLoopback(URI uri) {
            String host = uri.getHost();
            return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
        }

        private void validate() {
            require(!isBlank(clientId), "clientId é obrigatório.");
            require(!isBlank(clientSecret), "clientSecret é obrigatório.");
            require(environment != null || baseUrl != null, "environment é obrigatório quando baseUrl não é informada.");
            require(resilience != null, "resilience não pode ser nulo.");

            if (baseUrl != null)
                validateBaseUrl(baseUrl);

            validateResilience(resilience);
        }

        private static void validateBaseUrl(URI url) {
            require(url.isAbsolute() && url.getHost() != null, "baseUrl, quando informada, deve ser uma URI absoluta.");

            String scheme = url.getScheme().toLowerCase(Locale.ROOT);
            boolean https = "https".equals(scheme);
            boolean httpLoopback = "http".equals(scheme) && isLoopback(url);
            require(https || httpLoopback, "baseUrl deve usar https (http só é aceito para localhost e 127.0.0.1).");
        }

        private static void validateResilience(CredicarroResilienceOptions options) {
            require(options.getMaxRetryAttempts() >= 1, "resilience.maxRetryAttempts deve ser maior ou igual a 1.");
            require(isPositive(options.getTotalTimeout()), "resilience.totalTimeout deve ser maior que zero.");
            require(isPositive(options.getAttemptTimeout()), "resilience.attemptTimeout deve ser maior que zero.");
            require(options.getAttemptTimeout().compareTo(options.getTotalTimeout()) <= 0,
                    "resilience.attemptTimeout não pode ser maior que resilience.totalTimeout.");
            require(isPositive(options.getDocumentUploadAttemptTimeout()),
                    "resilience.documentUploadAttemptTimeout deve ser maior que zero.");
            require(!options.getRetryBaseDelay().isNegative(), "resilience.retryBaseDelay não pode ser negativo.");
            require(options.getCircuitBreakerFailureRatio() > 0 && options.getCircuitBreakerFailureRatio() <= 1,
                    "resilience.circuitBreakerFailureRatio deve estar entre 0 (exclusivo) e 1 (inclusivo).");
            require(options.getCircuitBreakerMinimumThroughput() >= 2,
                    "resilience.circuitBreakerMinimumThroughput deve ser maior ou igual a 2.");
            require(options.getCircuitBreakerSlidingWindowSize() >= 1,
                    "resilience.circuitBreakerSlidingWindowSize (segundos) deve ser maior ou igual a 1.");
            require(isPositive(options.getCircuitBreakerBreakDuration()),
                    "resilience.circuitBreakerBreakDuration deve ser maior que zero.");
        }

        private static boolean isPositive(Duration value) {
            return !value.isZero() && !value.isNegative();
        }

        private static void require(boolean valid, String message) {
            if (!valid)
                throw new CredicarroConfigurationException(message);
        }
    }
}
