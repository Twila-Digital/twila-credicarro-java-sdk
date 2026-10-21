package twila.credicarro.internal.http;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Interpretação do cabeçalho {@code Retry-After}: segundos ou data HTTP. Valores inválidos, zero ou negativos são
 * ignorados ({@code null}); valores absurdos são limitados a {@link #MAX} antes de virar duração.
 */
public final class RetryAfter {

    public static final Duration MAX = Duration.ofHours(24);

    private static final Pattern SECONDS = Pattern.compile("^\\d+$");

    private RetryAfter() {
    }

    public static Duration parse(String value) {
        return parse(value, Instant.now());
    }

    static Duration parse(String value, Instant now) {
        if (value == null)
            return null;

        String text = value.trim();
        if (text.isEmpty())
            return null;

        if (SECONDS.matcher(text).matches()) {
            // Mais de 9 dígitos já passa de 31 anos: limita sem converter para número.
            if (text.length() > 9)
                return MAX;

            long seconds = Long.parseLong(text);
            return seconds <= 0 ? null : min(Duration.ofSeconds(seconds));
        }

        try {
            Instant target = ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            Duration delta = Duration.between(now, target);
            return delta.isNegative() || delta.isZero() ? null : min(delta);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Duration min(Duration duration) {
        return duration.compareTo(MAX) > 0 ? MAX : duration;
    }
}
