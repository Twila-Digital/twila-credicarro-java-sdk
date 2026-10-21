package twila.credicarro.errors;

/**
 * Lançada quando a autenticação falha: credenciais inválidas ({@code 401 Integration.Authentication.InvalidCredentials}),
 * requisição de token inválida ({@code 400}), ou {@code 401} persistente após uma renovação de token.
 * A mensagem nunca contém o {@code clientSecret} nem o token.
 */
public final class CredicarroAuthenticationException extends CredicarroException {

    private final int statusCode;
    private final String errorCode;

    public CredicarroAuthenticationException(String message) {
        this(message, 0, null);
    }

    public CredicarroAuthenticationException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    /**
     * Código HTTP da resposta de autenticação, ou {@code 0} quando não houve resposta.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Campo {@code tipo} do ProblemDetails, quando a resposta trouxe corpo (ex.: {@code Integration.Authentication.InvalidCredentials}).
     */
    public String getErrorCode() {
        return errorCode;
    }
}
