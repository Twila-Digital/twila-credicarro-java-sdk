package twila.credicarro.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import twila.credicarro.internal.idempotency.IdempotencyClassifier;

class IdempotencyClassifierTest {

    private static final String ID = "bea8d060-f93d-48eb-b6c4-45f64483cdd9";

    @Test
    void automaticKeyOnTheDocumentedRoutes() {
        String[] routes = {
                "v1/customer", "v1/customer/address", "v1/customer/professional",
                "v1/proposal/simulation/financing", "v1/proposal/simulation/refinancing",
                "v2/proposal/simulation/financing", "v2/proposal/simulation/refinancing",
                "v1/proposal", "v2/proposal", "v2/proposal/" + ID + "/document"
        };

        for (String route : routes) {
            assertThat(IdempotencyClassifier.requiresAutomaticKey("POST", route)).as(route).isTrue();
            assertThat(IdempotencyClassifier.isRetrySafe("POST", route, true)).as(route + " com chave").isTrue();
            assertThat(IdempotencyClassifier.isRetrySafe("POST", route, false)).as(route + " sem chave").isFalse();
        }
    }

    @Test
    void unifiedNeverGetsAnAutomaticKeyAndIsOnlyRetrySafeWithAnExplicitOne() {
        assertThat(IdempotencyClassifier.requiresAutomaticKey("POST", "v1/customer/unified")).isFalse();
        assertThat(IdempotencyClassifier.acceptsIdempotencyKey("POST", "v1/customer/unified")).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v1/customer/unified", false)).isFalse();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v1/customer/unified", true)).isTrue();
    }

    @Test
    void webhookCreationIsNeverRetrySafe() {
        assertThat(IdempotencyClassifier.requiresAutomaticKey("POST", "v1/webhook")).isFalse();
        assertThat(IdempotencyClassifier.acceptsIdempotencyKey("POST", "v1/webhook")).isFalse();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v1/webhook", false)).isFalse();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v1/webhook", true)).isFalse();
    }

    @Test
    void replacementRoutesAreRetrySafeWithoutAKey() {
        assertThat(IdempotencyClassifier.requiresAutomaticKey("POST", "v2/proposal/" + ID + "/vehicle-detail")).isFalse();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v2/proposal/" + ID + "/vehicle-detail", false)).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v2/proposal/" + ID + "/deposit-account", false)).isTrue();
    }

    @Test
    void getAndTokenAreRetrySafeAndWebhookPutDeleteToo() {
        assertThat(IdempotencyClassifier.isRetrySafe("GET", "v2/proposal/" + ID, false)).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("POST", "v1/authentication/accesstoken", false)).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("PUT", "v1/webhook/" + ID, false)).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("DELETE", "v1/webhook/" + ID, false)).isTrue();
        assertThat(IdempotencyClassifier.isRetrySafe("PUT", "v2/proposal/" + ID, false)).isFalse();
    }

    @Test
    void queryStringAndLeadingSlashDoNotChangeTheDecision() {
        assertThat(IdempotencyClassifier.isRetrySafe("GET", "/v1/webhook?pagina_inicial=1&limite=10", false)).isTrue();
        assertThat(IdempotencyClassifier.requiresAutomaticKey("POST", "/v1/customer")).isTrue();
    }

    @Test
    void validatesExplicitKeys() {
        IdempotencyClassifier.validateKey("a");
        IdempotencyClassifier.validateKey(new String(new char[200]).replace('\0', 'x'));

        assertThatThrownBy(() -> IdempotencyClassifier.validateKey("chavé")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdempotencyClassifier.validateKey("a	b")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdempotencyClassifier.validateKey("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdempotencyClassifier.validateKey(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdempotencyClassifier.validateKey("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdempotencyClassifier.validateKey(new String(new char[201]).replace('\0', 'x')))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
