package twila.credicarro.internal.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corpo de {@code POST v1/authentication/accesstoken}. Não expõe o segredo em {@code toString}.
 */
public final class GenerateAccessTokenRequest {

    @JsonProperty("clientId")
    public final String clientId;

    @JsonProperty("clientSecret")
    public final String clientSecret;

    GenerateAccessTokenRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {
        return "GenerateAccessTokenRequest[clientId=" + clientId + "]";
    }
}
