package twila.credicarro.internal.http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.errors.CredicarroExceptionFactory;
import twila.credicarro.internal.auth.AccessTokenProvider;
import twila.credicarro.internal.idempotency.IdempotencyClassifier;
import twila.credicarro.internal.resilience.Deadline;
import twila.credicarro.internal.resilience.ResilienceExecutor;

public final class ApiRequestExecutor {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final byte[] EMPTY = new byte[0];

    private final OkHttpClient httpClient;
    private final URI baseUrl;
    private final AccessTokenProvider tokenProvider;
    private final ResilienceExecutor resilience;
    private final CredicarroResilienceOptions resilienceOptions;

    public ApiRequestExecutor(
            OkHttpClient httpClient,
            URI baseUrl,
            AccessTokenProvider tokenProvider,
            ResilienceExecutor resilience,
            CredicarroResilienceOptions resilienceOptions) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.tokenProvider = tokenProvider;
        this.resilience = resilience;
        this.resilienceOptions = resilienceOptions;
    }

    public ApiResponse get(String path) {
        return send("GET", path, null, null, false, resilienceOptions.getAttemptTimeout(), resilienceOptions.getTotalTimeout());
    }

    /**
     * {@code POST} com {@code Idempotency-Key} automática quando a rota a aceita (e a opção não foi desabilitada).
     */
    public ApiResponse post(String path, byte[] jsonBody) {
        return post(path, jsonBody, null);
    }

    /**
     * {@code POST} com {@code Idempotency-Key} explícita (usada no lugar da automática). Em
     * {@code v1/customer/unified} é a única forma de enviar a chave.
     */
    public ApiResponse post(String path, byte[] jsonBody, String explicitIdempotencyKey) {
        return send("POST", path, jsonBody, explicitIdempotencyKey, false,
                resilienceOptions.getAttemptTimeout(), resilienceOptions.getTotalTimeout());
    }

    /**
     * Envio de documento: a chave é obrigatória na API, então é sempre enviada (a explícita ou uma gerada aqui, a
     * mesma em todas as tentativas). O orçamento é {@code documentUploadAttemptTimeout × maxRetryAttempts}, para
     * que o limite total nunca corte uma tentativa antes do timeout dela.
     */
    public ApiResponse postDocument(String path, byte[] jsonBody, String explicitIdempotencyKey) {
        Duration attemptTimeout = resilienceOptions.getDocumentUploadAttemptTimeout();
        Duration budget;
        try {
            budget = attemptTimeout.multipliedBy(Math.max(1, resilienceOptions.getMaxRetryAttempts()));
        } catch (ArithmeticException ex) {
            budget = attemptTimeout;
        }

        return send("POST", path, jsonBody, explicitIdempotencyKey, true, attemptTimeout, budget);
    }

    public ApiResponse put(String path, byte[] jsonBody) {
        return send("PUT", path, jsonBody, null, false, resilienceOptions.getAttemptTimeout(), resilienceOptions.getTotalTimeout());
    }

    public ApiResponse delete(String path) {
        return send("DELETE", path, null, null, false, resilienceOptions.getAttemptTimeout(), resilienceOptions.getTotalTimeout());
    }

    public static void ensureSuccess(ApiResponse response) {
        if (response.statusCode() / 100 != 2)
            throw CredicarroExceptionFactory.fromResponse(response);
    }

    private ApiResponse send(
            String method,
            String path,
            byte[] jsonBody,
            String explicitKey,
            boolean forceKey,
            Duration attemptTimeout,
            Duration budget) {
        if (explicitKey != null)
            IdempotencyClassifier.validateKey(explicitKey);

        final String idempotencyKey = resolveKey(method, path, explicitKey, forceKey);
        boolean retrySafe = IdempotencyClassifier.isRetrySafe(method, path, idempotencyKey != null);
        final byte[] body = jsonBody != null ? jsonBody : EMPTY;

        return resilience.execute(retrySafe, Deadline.after(budget),
                deadline -> sendWithAuth(method, path, body, idempotencyKey, attemptTimeout, deadline));
    }

    private String resolveKey(String method, String path, String explicitKey, boolean forceKey) {
        if (explicitKey != null)
            return explicitKey;

        if (forceKey || (!resilienceOptions.isDisableAutomaticIdempotencyKey() && IdempotencyClassifier.requiresAutomaticKey(method, path)))
            return UUID.randomUUID().toString();

        return null;
    }

    private ApiResponse sendWithAuth(
            String method, String path, byte[] body, String idempotencyKey, Duration attemptTimeout, Deadline deadline) throws IOException {
        String token = tokenProvider.getToken(deadline);
        ApiResponse response = sendOnce(method, path, body, idempotencyKey, attemptTimeout, deadline, token);

        if (response.statusCode() != 401)
            return response;

        tokenProvider.invalidate(token);
        String newToken = tokenProvider.getToken(deadline);
        ApiResponse retried = sendOnce(method, path, body, idempotencyKey, attemptTimeout, deadline, newToken);

        if (retried.statusCode() != 401)
            return retried;

        throw CredicarroExceptionFactory.fromResponse(retried);
    }

    private ApiResponse sendOnce(
            String method, String path, byte[] body, String idempotencyKey, Duration attemptTimeout, Deadline deadline, String token)
            throws IOException {
        URI resolved = baseUrl.resolve(path);

        Request.Builder builder = new Request.Builder()
                .url(resolved.toString())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json");

        if (idempotencyKey != null)
            builder.header("Idempotency-Key", idempotencyKey);

        if ("GET".equals(method)) {
            builder.get();
        } else if ("DELETE".equals(method)) {
            builder.delete();
        } else if ("POST".equals(method)) {
            builder.post(RequestBody.create(body, JSON));
        } else if ("PUT".equals(method)) {
            builder.put(RequestBody.create(body, JSON));
        } else {
            throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        }

        OkHttpClient callClient = httpClient.newBuilder()
                .callTimeout(deadline.callTimeoutMillis(attemptTimeout), TimeUnit.MILLISECONDS)
                .build();

        try (Response response = callClient.newCall(builder.build()).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            return new ApiResponse(response.code(), responseBody, response.headers());
        }
    }
}
