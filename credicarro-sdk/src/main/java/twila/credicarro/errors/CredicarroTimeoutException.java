package twila.credicarro.errors;

/**
 * Lançada em timeout (por tentativa ou total), falha de rede ou quando o circuit breaker está aberto.
 */
public final class CredicarroTimeoutException extends CredicarroException {

    public CredicarroTimeoutException(String message) {
        super(message);
    }

    public CredicarroTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
