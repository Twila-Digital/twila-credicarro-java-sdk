package twila.credicarro.errors;

/**
 * Lançada para {@code 400 Bad Request} (campo inválido ou ausente). O campo vem em {@link #getField()}.
 */
public final class CredicarroValidationException extends CredicarroApiException {

    public CredicarroValidationException(String message, ProblemDetailsModel problemDetails, String rawBody) {
        super(message, 400, problemDetails, rawBody);
    }
}
