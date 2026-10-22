package twila.credicarro.internal.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import twila.credicarro.errors.CredicarroAuthenticationException;
import twila.credicarro.errors.CredicarroExceptionFactory;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.Routes;
import twila.credicarro.internal.resilience.Deadline;
import twila.credicarro.internal.resilience.ResilienceExecutor;
import twila.credicarro.serialization.CredicarroObjectMapper;

/**
 * Geração do token. O endpoint é seguro de repetir: falhas de rede e status transitórios têm retry próprio (sem
 * passar pelo circuit breaker), dentro do orçamento de tempo da chamada que precisa do token.
 */
public final class TokenApiClientImpl implements TokenApiClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final URI tokenEndpoint;
    private final Duration attemptTimeout;
    private final ResilienceExecutor resilience;

    public TokenApiClientImpl(OkHttpClient httpClient, URI baseUrl, Duration attemptTimeout, ResilienceExecutor resilience) {
        this.httpClient = httpClient;
        this.tokenEndpoint = baseUrl.resolve(Routes.ACCESS_TOKEN);
        this.attemptTimeout = attemptTimeout;
        this.resilience = resilience;
    }

    @Override
    public GenerateAccessTokenResponse generate(String clientId, String clientSecret, Deadline deadline) {
        final byte[] body;
        try {
            body = CredicarroObjectMapper.DEFAULT.writeValueAsBytes(new GenerateAccessTokenRequest(clientId, clientSecret));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao serializar o corpo da requisição de token.");
        }

        ApiResponse response = resilience.executeWithoutBreaker(true, deadline, d -> post(body, d));

        if (response.statusCode() / 100 != 2) {
            if (response.statusCode() == 400 || response.statusCode() == 401)
                throw CredicarroExceptionFactory.authenticationFromResponse(response);

            throw CredicarroExceptionFactory.fromResponse(response);
        }

        try {
            GenerateAccessTokenResponse result = CredicarroObjectMapper.DEFAULT.readValue(response.body(), GenerateAccessTokenResponse.class);

            if (result == null)
                throw new CredicarroAuthenticationException("A API do Credicarro retornou uma resposta vazia ao gerar o token de acesso.");

            return result;
        } catch (IOException ex) {
            throw new CredicarroAuthenticationException("A API do Credicarro retornou uma resposta inválida ao gerar o token de acesso.");
        }
    }

    private ApiResponse post(byte[] body, Deadline deadline) throws IOException {
        Request request = new Request.Builder()
                .url(tokenEndpoint.toString())
                .post(RequestBody.create(body, JSON))
                .build();

        OkHttpClient callClient = httpClient.newBuilder()
                .callTimeout(deadline.callTimeoutMillis(attemptTimeout), TimeUnit.MILLISECONDS)
                .build();

        try (Response raw = callClient.newCall(request).execute()) {
            String responseBody = raw.body() != null ? raw.body().string() : "";
            return new ApiResponse(raw.code(), responseBody, raw.headers());
        }
    }
}
