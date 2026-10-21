package twila.credicarro.errors;

import java.util.Collections;
import java.util.List;

/**
 * Modelo do {@code ProblemDetails} (campos em português: {@code tipo}, {@code titulo}, {@code status},
 * {@code detalhe}, {@code campo}, {@code erros}) devolvido pela API em respostas de erro. Também aceita o formato
 * padrão {@code type}/{@code title}/{@code detail} que a API usa em falhas inesperadas.
 * Campos ausentes, nulos ou desconhecidos nunca causam falha de leitura.
 */
public final class ProblemDetailsModel {

    private final String type;
    private final String title;
    private final Integer status;
    private final String detail;
    private final String field;
    private final List<CredicarroApiError> errors;

    public ProblemDetailsModel(String type, String title, Integer status, String detail, String field, List<CredicarroApiError> errors) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.field = field;
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.<CredicarroApiError>emptyList();
    }

    public static ProblemDetailsModel empty() {
        return new ProblemDetailsModel(null, null, null, null, null, null);
    }

    /**
     * {@code tipo} (ou {@code type}): código estável do erro, string opaca.
     */
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Integer getStatus() {
        return status;
    }

    /**
     * Pode conter dados pessoais — nunca é incluído na mensagem da exceção.
     */
    public String getDetail() {
        return detail;
    }

    public String getField() {
        return field;
    }

    public List<CredicarroApiError> getErrors() {
        return errors;
    }
}
