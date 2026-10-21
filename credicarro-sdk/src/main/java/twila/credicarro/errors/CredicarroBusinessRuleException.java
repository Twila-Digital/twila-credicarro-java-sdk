package twila.credicarro.errors;

/**
 * Lançada para {@code 422 Unprocessable Entity} (regra de negócio não atendida). Trate pelo {@link #getErrorCode()}.
 */
public final class CredicarroBusinessRuleException extends CredicarroApiException {

    public CredicarroBusinessRuleException(String message, ProblemDetailsModel problemDetails, String rawBody) {
        super(message, 422, problemDetails, rawBody);
    }
}
