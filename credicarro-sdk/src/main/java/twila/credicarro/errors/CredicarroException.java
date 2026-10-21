package twila.credicarro.errors;

/**
 * Exceção base para todos os erros lançados pelo SDK do Credicarro.
 */
public abstract class CredicarroException extends RuntimeException {

    protected CredicarroException(String message) {
        super(message);
    }

    protected CredicarroException(String message, Throwable cause) {
        super(message, cause);
    }
}
