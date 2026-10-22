package twila.credicarro.internal.auth;

import twila.credicarro.internal.resilience.Deadline;

public interface TokenApiClient {

    GenerateAccessTokenResponse generate(String clientId, String clientSecret, Deadline deadline);
}
