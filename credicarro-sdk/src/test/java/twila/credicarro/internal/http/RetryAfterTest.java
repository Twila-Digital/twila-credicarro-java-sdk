package twila.credicarro.internal.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RetryAfterTest {

    private static final Instant NOW = Instant.parse("2026-10-19T12:00:00Z");

    @Test
    void parsesSecondsAndHttpDates() {
        assertThat(RetryAfter.parse("30", NOW)).isEqualTo(Duration.ofSeconds(30));
        assertThat(RetryAfter.parse(" 7 ", NOW)).isEqualTo(Duration.ofSeconds(7));
        assertThat(RetryAfter.parse("Mon, 19 Oct 2026 12:00:45 GMT", NOW)).isEqualTo(Duration.ofSeconds(45));
    }

    @Test
    void ignoresInvalidZeroNegativeAndPastValues() {
        for (String value : new String[] {null, "", "  ", "abc", "-5", "0", "00", "1.5", "Mon, 19 Oct 2026 11:00:00 GMT", "Mon, 19 Oct 2026 12:00:00 GMT"})
            assertThat(RetryAfter.parse(value, NOW)).as(String.valueOf(value)).isNull();
    }

    @Test
    void capsAbsurdValuesBeforeConvertingToADuration() {
        assertThat(RetryAfter.parse("99999999999999999999999999", NOW)).isEqualTo(RetryAfter.MAX);
        assertThat(RetryAfter.parse("999999999", NOW)).isEqualTo(RetryAfter.MAX);
        assertThat(RetryAfter.parse("Fri, 01 Jan 2100 00:00:00 GMT", NOW)).isEqualTo(RetryAfter.MAX);
    }
}
