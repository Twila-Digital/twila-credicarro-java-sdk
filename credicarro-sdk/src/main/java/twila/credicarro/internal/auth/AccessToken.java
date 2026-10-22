package twila.credicarro.internal.auth;

import java.time.Instant;

final class AccessToken {

    private final String value;
    private final Instant expiresAt;

    AccessToken(String value, Instant expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
    }

    String value() {
        return value;
    }

    /**
     * O {@code expira_em} da API já vem com 60 segundos de margem; o token vale enquanto {@code now} for anterior a ele.
     */
    boolean isValidAt(Instant now) {
        return now.isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "AccessToken[expiresAt=" + expiresAt + "]";
    }
}
