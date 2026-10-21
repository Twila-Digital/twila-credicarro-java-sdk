package twila.credicarro.internal.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/**
 * Resposta de {@code POST v1/authentication/accesstoken} (schema {@code authenticationResponse}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GenerateAccessTokenResponse {

    @JsonProperty("token_de_acesso")
    public String accessToken;

    @JsonProperty("tipo_de_token")
    public String tokenType;

    @JsonProperty("expira_em_segundos")
    public Integer expiresInSeconds;

    @JsonProperty("expira_em")
    public OffsetDateTime expiresAt;

    @JsonProperty("escopo")
    public String scope;

    @Override
    public String toString() {
        return "GenerateAccessTokenResponse[tokenType=" + tokenType + ", expiresAt=" + expiresAt + "]";
    }
}
