package twila.credicarro.internal.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.errors.CredicarroException;
import twila.credicarro.errors.CredicarroExceptionFactory;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.RetryAfter;

/**
 * Retry com backoff, circuit breaker e orçamento de tempo. Tudo roda na thread do chamador: o orçamento é aplicado
 * como {@code callTimeout} de cada requisição e um {@link Deadline} é conferido entre as tentativas — não há pool de
 * threads nem tarefa presa após timeout.
 */
public final class ResilienceExecutor {

    private final CredicarroResilienceOptions options;
    private final CircuitBreaker circuitBreaker;

    /**
     * Uma tentativa HTTP completa. Recebe o orçamento para calcular o {@code callTimeout}.
     */
    public interface Attempt {
        ApiResponse call(Deadline deadline) throws IOException;
    }

    public ResilienceExecutor(CredicarroResilienceOptions options) {
        this.options = options;
        this.circuitBreaker = CircuitBreaker.of("credicarro-api", buildCircuitBreakerConfig(options));
    }

    /**
     * Executa com retry (se {@code retrySafe}) e circuit breaker.
     */
    public ApiResponse execute(boolean retrySafe, Deadline deadline, Attempt attempt) {
        return run(retrySafe, deadline, true, attempt);
    }

    /**
     * Executa com retry, mas sem passar pelo circuit breaker (usado pela geração do token, que acontece dentro de uma
     * tentativa que já detém uma permissão do breaker).
     */
    public ApiResponse executeWithoutBreaker(boolean retrySafe, Deadline deadline, Attempt attempt) {
        return run(retrySafe, deadline, false, attempt);
    }

    private ApiResponse run(boolean retrySafe, Deadline deadline, boolean useBreaker, Attempt attempt) {
        final int maxAttempts = retrySafe ? Math.max(1, options.getMaxRetryAttempts()) : 1;

        for (int number = 1; ; number++) {
            if (deadline.isExpired())
                throw new CredicarroTimeoutException("A requisição excedeu o tempo limite total configurado.");

            Outcome outcome = runOnce(deadline, useBreaker, attempt);
            boolean attemptsLeft = number < maxAttempts;

            if (outcome.failure != null) {
                awaitNetworkRetry(outcome.failure, attemptsLeft, number, deadline);
                continue;
            }

            Duration delay = attemptsLeft ? delayBeforeRetry(outcome.response, number, deadline) : null;
            if (delay == null)
                return outcome.response;

            sleep(delay);
        }
    }

    /**
     * Espera o backoff antes de repetir uma falha de rede, ou lança a falha (sanitizada) quando não há mais tentativas
     * ou a espera passaria do orçamento.
     */
    private void awaitNetworkRetry(IOException failure, boolean attemptsLeft, int number, Deadline deadline) {
        if (attemptsLeft) {
            Duration delay = backoff(number);
            if (delay.compareTo(deadline.remaining()) < 0) {
                sleep(delay);
                return;
            }
        }

        throw networkFailure(failure);
    }

    /**
     * Espera até a próxima tentativa de uma resposta transitória, ou {@code null} quando a resposta deve ser devolvida
     * já (não transitória, ou sem orçamento para esperar).
     */
    private Duration delayBeforeRetry(ApiResponse response, int number, Deadline deadline) {
        if (!TransientFailureClassifier.isTransientResponse(response, options))
            return null;

        Duration retryAfter = RetryAfter.parse(response.headers().get("Retry-After"));
        Duration remaining = deadline.remaining();

        if (retryAfter != null && retryAfter.compareTo(remaining) >= 0)
            throw CredicarroExceptionFactory.fromResponse(response);

        Duration delay = retryAfter != null ? retryAfter : backoff(number);
        return delay.compareTo(remaining) < 0 ? delay : null;
    }

    private Outcome runOnce(Deadline deadline, boolean useBreaker, Attempt attempt) {
        boolean permitted = false;
        boolean recorded = false;
        long start = System.nanoTime();

        try {
            if (useBreaker) {
                try {
                    circuitBreaker.acquirePermission();
                } catch (CallNotPermittedException ex) {
                    throw new CredicarroTimeoutException("O circuit breaker está aberto — chamadas recentes falharam de forma consistente.");
                }
                permitted = true;
            }

            ApiResponse response = null;
            IOException failure = null;
            try {
                response = attempt.call(deadline);
            } catch (IOException ex) {
                failure = ex;
            }

            if (permitted) {
                long elapsed = System.nanoTime() - start;
                if (failure != null || TransientFailureClassifier.isTransientResponse(response, options))
                    circuitBreaker.onError(elapsed, TimeUnit.NANOSECONDS, new TransientFailure());
                else
                    circuitBreaker.onSuccess(elapsed, TimeUnit.NANOSECONDS);
                recorded = true;
            }

            return new Outcome(response, failure);
        } catch (CredicarroTimeoutException ex) {
            if (permitted && !recorded) {
                circuitBreaker.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, new TransientFailure());
                recorded = true;
            }
            throw ex;
        } catch (CredicarroException ex) {
            if (permitted && !recorded) {
                circuitBreaker.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                recorded = true;
            }
            throw ex;
        } finally {
            if (permitted && !recorded)
                circuitBreaker.releasePermission();
        }
    }

    private Duration backoff(int attemptNumber) {
        long base = options.getRetryBaseDelay().toMillis();
        long exponential = base * (1L << Math.min(20, Math.max(0, attemptNumber - 1)));
        double jitter = 0.5 + ThreadLocalRandom.current().nextDouble();
        return Duration.ofMillis((long) (exponential * jitter));
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(Math.max(0, delay.toMillis()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CredicarroTimeoutException("A requisição foi interrompida.");
        }
    }

    /**
     * Falha de rede ou timeout, com causa sanitizada: só o tipo da falha original, sem mensagem, host, headers ou corpo.
     */
    private static CredicarroTimeoutException networkFailure(IOException failure) {
        return new CredicarroTimeoutException("Falha de rede ou timeout ao chamar a API do Credicarro.",
                new SanitizedNetworkException(failure.getClass().getName()));
    }

    private static CircuitBreakerConfig buildCircuitBreakerConfig(CredicarroResilienceOptions options) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold((float) (options.getCircuitBreakerFailureRatio() * 100))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(options.getCircuitBreakerSlidingWindowSize())
                .minimumNumberOfCalls(options.getCircuitBreakerMinimumThroughput())
                .waitDurationInOpenState(options.getCircuitBreakerBreakDuration())
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
    }

    private static final class Outcome {
        final ApiResponse response;
        final IOException failure;

        Outcome(ApiResponse response, IOException failure) {
            this.response = response;
            this.failure = failure;
        }
    }

    private static final class TransientFailure extends RuntimeException {
        TransientFailure() {
            super("falha transitória", null, false, false);
        }
    }

    /**
     * Causa exposta ao usuário: não referencia objetos da biblioteca HTTP.
     */
    private static final class SanitizedNetworkException extends IOException {
        SanitizedNetworkException(String type) {
            super(type);
        }
    }
}
