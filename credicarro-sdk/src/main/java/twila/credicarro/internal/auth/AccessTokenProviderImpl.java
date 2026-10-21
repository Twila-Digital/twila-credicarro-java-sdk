package twila.credicarro.internal.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import twila.credicarro.errors.CredicarroAuthenticationException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.internal.resilience.Deadline;

public final class AccessTokenProviderImpl implements AccessTokenProvider {

    private static final long FALLBACK_MARGIN_SECONDS = 60;

    private final TokenApiClient tokenApiClient;
    private final String clientId;
    private final String clientSecret;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile AccessToken cached;

    public AccessTokenProviderImpl(TokenApiClient tokenApiClient, String clientId, String clientSecret) {
        this(tokenApiClient, clientId, clientSecret, Clock.systemUTC());
    }

    public AccessTokenProviderImpl(TokenApiClient tokenApiClient, String clientId, String clientSecret, Clock clock) {
        this.tokenApiClient = tokenApiClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clock = clock;
    }

    @Override
    public String getToken(Deadline deadline) {
        AccessToken current = cached;
        if (current != null && current.isValidAt(clock.instant()))
            return current.value();

        acquire(deadline);
        try {
            current = cached;
            if (current != null && current.isValidAt(clock.instant()))
                return current.value();

            GenerateAccessTokenResponse response = tokenApiClient.generate(clientId, clientSecret, deadline);

            if (response.accessToken == null || response.accessToken.isEmpty())
                throw new CredicarroAuthenticationException("A API do Credicarro retornou uma resposta sem token de acesso.");

            AccessToken fresh = new AccessToken(response.accessToken, expirationOf(response));
            cached = fresh;
            return fresh.value();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Espera o lock só até o fim do orçamento da própria chamada: outra thread renovando o token (com retry) não
     * pode prender esta além do seu {@link Deadline}.
     */
    private void acquire(Deadline deadline) {
        try {
            if (!lock.tryLock(deadline.remaining().toNanos(), TimeUnit.NANOSECONDS))
                throw new CredicarroTimeoutException("A requisição excedeu o tempo limite total configurado.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CredicarroTimeoutException("A requisição foi interrompida.");
        }
    }

    @Override
    public void invalidate(String rejectedToken) {
        lock.lock();
        try {
            AccessToken current = cached;
            if (current != null && current.value().equals(rejectedToken))
                cached = null;
        } finally {
            lock.unlock();
        }
    }

    private Instant expirationOf(GenerateAccessTokenResponse response) {
        if (response.expiresAt != null)
            return response.expiresAt.toInstant();

        long seconds = response.expiresInSeconds != null ? response.expiresInSeconds : 0;
        // Sem expira_em: agora + max(segundos / 2, segundos - 60).
        return clock.instant().plusSeconds(Math.max(seconds / 2, seconds - FALLBACK_MARGIN_SECONDS));
    }
}
