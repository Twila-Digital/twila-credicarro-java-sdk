package twila.credicarro.errors;

/**
 * Lançada quando a configuração do client é inválida. A mensagem identifica o campo, nunca o valor.
 */
public final class CredicarroConfigurationException extends CredicarroException {

    public CredicarroConfigurationException(String message) {
        super(message);
    }
}
