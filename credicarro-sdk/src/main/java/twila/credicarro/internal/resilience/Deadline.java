package twila.credicarro.internal.resilience;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import twila.credicarro.errors.CredicarroTimeoutException;

/**
 * Orçamento de tempo de uma chamada lógica (todas as tentativas, esperas e a geração do token). Usa o relógio
 * monotônico. Cada requisição HTTP recebe {@code min(attemptTimeout, restante)} como {@code callTimeout}.
 */
public final class Deadline {

    private final long deadlineNanos;

    private Deadline(long deadlineNanos) {
        this.deadlineNanos = deadlineNanos;
    }

    public static Deadline after(Duration budget) {
        long nanos;
        try {
            nanos = budget.toNanos();
        } catch (ArithmeticException ex) {
            nanos = Long.MAX_VALUE / 4;
        }

        return new Deadline(System.nanoTime() + nanos);
    }

    /**
     * Tempo restante (zero quando esgotado).
     */
    public Duration remaining() {
        long left = deadlineNanos - System.nanoTime();
        return left > 0 ? Duration.ofNanos(left) : Duration.ZERO;
    }

    public boolean isExpired() {
        return deadlineNanos - System.nanoTime() <= 0;
    }

    /**
     * Timeout de uma requisição HTTP: o menor entre o da tentativa e o restante do orçamento.
     *
     * @throws CredicarroTimeoutException se o orçamento já acabou
     */
    public long callTimeoutMillis(Duration attemptTimeout) {
        long remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0)
            throw new CredicarroTimeoutException("A requisição excedeu o tempo limite total configurado.");

        long attemptNanos;
        try {
            attemptNanos = attemptTimeout.toNanos();
        } catch (ArithmeticException ex) {
            attemptNanos = Long.MAX_VALUE;
        }

        long nanos = Math.min(remainingNanos, attemptNanos);
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(nanos));
    }
}
