package twila.credicarro.errors;

import java.time.Duration;

/**
 * Lançada para {@code 429 Too Many Requests}.
 */
public final class CredicarroRateLimitException extends CredicarroApiException {

    private final Duration retryAfter;

    public CredicarroRateLimitException(String message, ProblemDetailsModel problemDetails, String rawBody, Duration retryAfter) {
        super(message, 429, problemDetails, rawBody);
        this.retryAfter = retryAfter;
    }

    /**
     * Cabeçalho {@code Retry-After} da resposta, quando presente.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
