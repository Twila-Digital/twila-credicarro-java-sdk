package twila.credicarro.internal.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroAuthenticationException;
import twila.credicarro.internal.resilience.Deadline;

class AccessTokenProviderTest {

    private static final Instant START = Instant.parse("2026-10-19T12:00:00Z");

    /**
     * Relógio mutável para avançar o tempo sem dormir.
     */
    private static final class MutableClock extends Clock {
        private Instant now = START;

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static Deadline deadline() {
        return Deadline.after(Duration.ofSeconds(30));
    }

    private static GenerateAccessTokenResponse token(String value, OffsetDateTime expiresAt, Integer expiresInSeconds) {
        GenerateAccessTokenResponse response = new GenerateAccessTokenResponse();
        response.accessToken = value;
        response.expiresAt = expiresAt;
        response.expiresInSeconds = expiresInSeconds;
        return response;
    }

    @Test
    void reusesTheCachedTokenUntilExpiraEm() {
        MutableClock clock = new MutableClock();
        AtomicInteger calls = new AtomicInteger();
        TokenApiClient api = (id, secret, deadline) -> token("token-" + calls.incrementAndGet(), OffsetDateTime.ofInstant(START.plusSeconds(3540), ZoneOffset.UTC), 3600);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret", clock);

        assertThat(provider.getToken(deadline())).isEqualTo("token-1");
        clock.advance(Duration.ofSeconds(3539));
        assertThat(provider.getToken(deadline())).isEqualTo("token-1");
        assertThat(calls.get()).isEqualTo(1);

        clock.advance(Duration.ofSeconds(1));
        assertThat(provider.getToken(deadline())).isEqualTo("token-2");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void withoutExpiraEmUsesExpiraEmSegundosMinusSixtySeconds() {
        MutableClock clock = new MutableClock();
        AtomicInteger calls = new AtomicInteger();
        TokenApiClient api = (id, secret, deadline) -> token("token-" + calls.incrementAndGet(), null, 3600);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret", clock);

        provider.getToken(deadline());
        clock.advance(Duration.ofSeconds(3599 - 60));
        provider.getToken(deadline());
        assertThat(calls.get()).isEqualTo(1);

        clock.advance(Duration.ofSeconds(1));
        provider.getToken(deadline());
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void shortTokensWithoutExpiraEmAreValidForHalfOfTheirLifetime() {
        MutableClock clock = new MutableClock();
        AtomicInteger calls = new AtomicInteger();
        TokenApiClient api = (id, secret, deadline) -> token("token-" + calls.incrementAndGet(), null, 100);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret", clock);

        provider.getToken(deadline());
        clock.advance(Duration.ofSeconds(49));
        provider.getToken(deadline());
        assertThat(calls.get()).isEqualTo(1);

        clock.advance(Duration.ofSeconds(1));
        provider.getToken(deadline());
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void invalidateOnlyDropsTheCacheWhenTheRejectedTokenIsStillTheCurrentOne() {
        MutableClock clock = new MutableClock();
        AtomicInteger calls = new AtomicInteger();
        TokenApiClient api = (id, secret, deadline) -> token("token-" + calls.incrementAndGet(), null, 3600);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret", clock);

        String first = provider.getToken(deadline());
        provider.invalidate(first);
        String second = provider.getToken(deadline());
        assertThat(second).isNotEqualTo(first);

        // Um token já substituído sendo rejeitado não deve derrubar o token novo.
        provider.invalidate(first);
        assertThat(provider.getToken(deadline())).isEqualTo(second);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void renewsWithASingleRequestEvenWithManyConcurrentCallers()  throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        TokenApiClient api = (id, secret, deadline) -> {
            calls.incrementAndGet();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            return token("token-unico", null, 3600);
        };
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret");

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++)
                futures.add(pool.submit(() -> provider.getToken(deadline())));

            Thread.sleep(200);
            release.countDown();

            for (Future<String> future : futures)
                assertThat(future.get(5, TimeUnit.SECONDS)).isEqualTo("token-unico");
        } finally {
            pool.shutdownNow();
        }

        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void failsWhenTheApiReturnsNoToken() {
        TokenApiClient api = (id, secret, deadline) -> token(null, null, 3600);
        AccessTokenProviderImpl provider = new AccessTokenProviderImpl(api, "id", "secret");

        assertThatThrownBy(() -> provider.getToken(deadline())).isInstanceOf(CredicarroAuthenticationException.class);
    }
}
