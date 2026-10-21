package twila.credicarro.errors;

import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Item da lista {@code erros} do ProblemDetails ({@code codigo}, {@code mensagem}, {@code campo}).
 */
@Value
public class CredicarroApiError {
    String code;
    String message;
    String field;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CredicarroApiError", "code", code, "message", Masking.mask(message), "field", field);
    }
}
