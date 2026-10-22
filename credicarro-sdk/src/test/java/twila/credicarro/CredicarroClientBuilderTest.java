package twila.credicarro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import twila.credicarro.config.CredicarroEnvironment;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.errors.CredicarroConfigurationException;

class CredicarroClientBuilderTest {

    @Test
    void requiresClientId() {
        assertThatThrownBy(() -> CredicarroClient.builder().clientSecret("secret").build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("clientId");
    }

    @Test
    void requiresClientSecret() {
        assertThatThrownBy(() -> CredicarroClient.builder().clientId("id").build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("clientSecret");
    }

    @Test
    void rejectsRelativeBaseUrl() {
        assertThatThrownBy(() -> builder().baseUrl(URI.create("/relative")).build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void rejectsPlainHttpBaseUrlForRemoteHosts() {
        assertThatThrownBy(() -> builder().baseUrl(URI.create("http://api.credicarro.com.br/integration")).build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("https");
    }

    @Test
    void acceptsHttpsAndLoopbackBaseUrls() {
        builder().baseUrl(URI.create("https://api.exemplo.com.br/integration")).build().close();
        builder().baseUrl(URI.create("http://localhost:8080/integration")).build().close();
        builder().baseUrl(URI.create("http://127.0.0.1:8080/integration")).build().close();
    }

    @Test
    void rejectsAttemptTimeoutGreaterThanTotalTimeout() {
        CredicarroResilienceOptions resilience = CredicarroResilienceOptions.builder()
                .totalTimeout(Duration.ofSeconds(5))
                .attemptTimeout(Duration.ofSeconds(10))
                .build();

        assertThatThrownBy(() -> builder().resilience(resilience).build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("attemptTimeout");
    }

    @Test
    void rejectsMaxRetryAttemptsBelowOne() {
        CredicarroResilienceOptions resilience = CredicarroResilienceOptions.builder().maxRetryAttempts(0).build();

        assertThatThrownBy(() -> builder().resilience(resilience).build())
                .isInstanceOf(CredicarroConfigurationException.class)
                .hasMessageContaining("maxRetryAttempts");
    }

    @Test
    void environmentsPointToTheDocumentedUrls() {
        assertThat(CredicarroEnvironment.STAGING.toBaseUri().toString()).isEqualTo("https://api.staging.credicarro.com.br/integration/");
        assertThat(CredicarroEnvironment.PRODUCTION.toBaseUri().toString()).isEqualTo("https://api.credicarro.com.br/integration/");
    }

    @Test
    void resilienceDefaultsFollowTheContract() {
        CredicarroResilienceOptions defaults = CredicarroResilienceOptions.builder().build();

        assertThat(defaults.getTotalTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(defaults.getAttemptTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(defaults.getDocumentUploadAttemptTimeout()).isEqualTo(Duration.ofSeconds(60));
        assertThat(defaults.getMaxRetryAttempts()).isEqualTo(3);
        assertThat(defaults.getRetryBaseDelay()).isEqualTo(Duration.ofMillis(500));
        assertThat(defaults.getCircuitBreakerFailureRatio()).isEqualTo(0.5);
        assertThat(defaults.getCircuitBreakerSlidingWindowSize()).isEqualTo(30);
        assertThat(defaults.getCircuitBreakerMinimumThroughput()).isEqualTo(10);
        assertThat(defaults.getCircuitBreakerBreakDuration()).isEqualTo(Duration.ofSeconds(15));
        assertThat(defaults.isRetryOn500()).isTrue();
        assertThat(defaults.isDisableAutomaticIdempotencyKey()).isFalse();
    }

    @Test
    void buildsSuccessfullyAndNeverExposesTheSecret() {
        try (CredicarroClient client = builder().build()) {
            assertThat(client.customers()).isNotNull();
            assertThat(client.simulations()).isNotNull();
            assertThat(client.proposals()).isNotNull();
            assertThat(client.webhooks()).isNotNull();
            assertThat(client.v1().simulations()).isNotNull();
            assertThat(client.v1().proposals()).isNotNull();
            assertThat(client.toString()).doesNotContain("secret-super-sigiloso");
        }
    }

    private static CredicarroClient.Builder builder() {
        return CredicarroClient.builder().clientId("id").clientSecret("secret-super-sigiloso");
    }
}
