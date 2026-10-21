package twila.credicarro;

import java.net.URI;
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
            if (isBlank(clientId))
                throw new CredicarroConfigurationException("clientId é obrigatório.");

            if (isBlank(clientSecret))
                throw new CredicarroConfigurationException("clientSecret é obrigatório.");

            if (environment == null && baseUrl == null)
                throw new CredicarroConfigurationException("environment é obrigatório quando baseUrl não é informada.");

            if (resilience == null)
                throw new CredicarroConfigurationException("resilience não pode ser nulo.");

            if (baseUrl != null) {
                if (!baseUrl.isAbsolute() || baseUrl.getHost() == null)
                    throw new CredicarroConfigurationException("baseUrl, quando informada, deve ser uma URI absoluta.");

                String scheme = baseUrl.getScheme().toLowerCase(Locale.ROOT);
                boolean https = "https".equals(scheme);
                boolean httpLoopback = "http".equals(scheme) && isLoopback(baseUrl);
                if (!https && !httpLoopback)
                    throw new CredicarroConfigurationException("baseUrl deve usar https (http só é aceito para localhost e 127.0.0.1).");
            }

            if (resilience.getMaxRetryAttempts() < 1)
                throw new CredicarroConfigurationException("resilience.maxRetryAttempts deve ser maior ou igual a 1.");

            if (resilience.getTotalTimeout().isZero() || resilience.getTotalTimeout().isNegative())
                throw new CredicarroConfigurationException("resilience.totalTimeout deve ser maior que zero.");

            if (resilience.getAttemptTimeout().isZero() || resilience.getAttemptTimeout().isNegative())
                throw new CredicarroConfigurationException("resilience.attemptTimeout deve ser maior que zero.");

            if (resilience.getAttemptTimeout().compareTo(resilience.getTotalTimeout()) > 0)
                throw new CredicarroConfigurationException("resilience.attemptTimeout não pode ser maior que resilience.totalTimeout.");

            if (resilience.getDocumentUploadAttemptTimeout().isZero() || resilience.getDocumentUploadAttemptTimeout().isNegative())
                throw new CredicarroConfigurationException("resilience.documentUploadAttemptTimeout deve ser maior que zero.");

            if (resilience.getRetryBaseDelay().isNegative())
                throw new CredicarroConfigurationException("resilience.retryBaseDelay não pode ser negativo.");

            if (resilience.getCircuitBreakerFailureRatio() <= 0 || resilience.getCircuitBreakerFailureRatio() > 1)
                throw new CredicarroConfigurationException("resilience.circuitBreakerFailureRatio deve estar entre 0 (exclusivo) e 1 (inclusivo).");

            if (resilience.getCircuitBreakerMinimumThroughput() < 2)
                throw new CredicarroConfigurationException("resilience.circuitBreakerMinimumThroughput deve ser maior ou igual a 2.");

            if (resilience.getCircuitBreakerSlidingWindowSize() < 1)
                throw new CredicarroConfigurationException("resilience.circuitBreakerSlidingWindowSize (segundos) deve ser maior ou igual a 1.");

            if (resilience.getCircuitBreakerBreakDuration().isZero() || resilience.getCircuitBreakerBreakDuration().isNegative())
                throw new CredicarroConfigurationException("resilience.circuitBreakerBreakDuration deve ser maior que zero.");
        }
    }
}
