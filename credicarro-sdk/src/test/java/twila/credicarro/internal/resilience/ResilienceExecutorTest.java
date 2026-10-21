package twila.credicarro.internal.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import twila.credicarro.config.CredicarroResilienceOptions;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.internal.http.ApiResponse;

class ResilienceExecutorTest {

    private static ApiResponse response(int status) {
        return new ApiResponse(status, "", Headers.of());
    }

    private static CredicarroResilienceOptions options() {
        return CredicarroResilienceOptions.builder()
                .maxRetryAttempts(1)
                .retryBaseDelay(Duration.ofMillis(5))
                .circuitBreakerMinimumThroughput(2)
                .circuitBreakerSlidingWindowSize(10)
                .circuitBreakerFailureRatio(0.5)
                .circuitBreakerBreakDuration(Duration.ofMillis(200))
                .build();
    }

    private static Deadline deadline() {
        return Deadline.after(Duration.ofSeconds(10));
    }

    @Test
    void halfOpenTrialIsReleasedWhenTheAttemptThrowsAnUnexpectedException() throws Exception {
        ResilienceExecutor executor = new ResilienceExecutor(options());

        executor.execute(false, deadline(), d -> response(503));
        executor.execute(false, deadline(), d -> response(503));
        assertThatThrownBy(() -> executor.execute(false, deadline(), d -> response(200)))
                .isInstanceOf(CredicarroTimeoutException.class).hasMessageContaining("circuit breaker");

        Thread.sleep(350);

        // Meia-abertura: a tentativa explode com um erro inesperado e não pode deixar o breaker preso.
        assertThatThrownBy(() -> executor.execute(false, deadline(), d -> {
            throw new IllegalStateException("bug");
        })).isInstanceOf(IllegalStateException.class);

        AtomicInteger calls = new AtomicInteger();
        ApiResponse ok = executor.execute(false, deadline(), d -> {
            calls.incrementAndGet();
            return response(200);
        });

        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void halfOpenTrialThatSucceedsClosesTheBreaker() throws Exception {
        ResilienceExecutor executor = new ResilienceExecutor(options());

        executor.execute(false, deadline(), d -> response(503));
        executor.execute(false, deadline(), d -> response(503));
        Thread.sleep(350);

        assertThat(executor.execute(false, deadline(), d -> response(200)).statusCode()).isEqualTo(200);
        assertThat(executor.execute(false, deadline(), d -> response(200)).statusCode()).isEqualTo(200);
    }

    @Test
    void ioFailuresAreRetriedOnlyWhenSafeAndThenBecomeATimeout() {
        CredicarroResilienceOptions retrying = CredicarroResilienceOptions.builder().maxRetryAttempts(3).retryBaseDelay(Duration.ofMillis(5)).build();
        ResilienceExecutor executor = new ResilienceExecutor(retrying);
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute(true, deadline(), d -> {
            calls.incrementAndGet();
            throw new IOException("host secreto.exemplo");
        })).isInstanceOf(CredicarroTimeoutException.class)
                .satisfies(ex -> assertThat(String.valueOf(ex.getCause().getMessage())).doesNotContain("secreto.exemplo"));

        assertThat(calls.get()).isEqualTo(3);

        calls.set(0);
        assertThatThrownBy(() -> executor.execute(false, deadline(), d -> {
            calls.incrementAndGet();
            throw new IOException("falha");
        })).isInstanceOf(CredicarroTimeoutException.class);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void expiredBudgetStopsBeforeAnotherAttempt() {
        ResilienceExecutor executor = new ResilienceExecutor(options());

        assertThatThrownBy(() -> executor.execute(false, Deadline.after(Duration.ZERO), d -> response(200)))
                .isInstanceOf(CredicarroTimeoutException.class);
    }
}
