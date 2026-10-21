package twila.credicarro.errors;

import java.util.List;

/**
 * Lançada para qualquer resposta de erro HTTP da API do Credicarro (400/403/404/409/422/5xx).
 * A mensagem contém somente o status, o {@code tipo} e o {@code titulo}; o {@code detalhe} e o corpo bruto ficam em
 * propriedades porque podem conter dados pessoais.
 */
public class CredicarroApiException extends CredicarroException {

    private final int statusCode;
    private final ProblemDetailsModel problemDetails;
    private final String rawBody;

    public CredicarroApiException(String message, int statusCode, ProblemDetailsModel problemDetails, String rawBody) {
        super(message);
        this.statusCode = statusCode;
        this.problemDetails = problemDetails != null ? problemDetails : ProblemDetailsModel.empty();
        this.rawBody = rawBody != null ? rawBody : "";
    }

    /**
     * Código HTTP da resposta.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Campo {@code tipo} do ProblemDetails (ex.: {@code Store.Integration.Intermediation}): string opaca, não um enum fechado.
     */
    public String getErrorCode() {
        return problemDetails.getType();
    }

    public String getTitle() {
        return problemDetails.getTitle();
    }

    /**
     * Campo {@code detalhe}: para logs e suporte; pode conter dados pessoais.
     */
    public String getDetail() {
        return problemDetails.getDetail();
    }

    /**
     * Campo {@code campo}: campo da requisição relacionado ao erro, quando houver.
     */
    public String getField() {
        return problemDetails.getField();
    }

    /**
     * Itens de {@code erros} (vazia quando a API não os retornou).
     */
    public List<CredicarroApiError> getErrors() {
        return problemDetails.getErrors();
    }

    public ProblemDetailsModel getProblemDetails() {
        return problemDetails;
    }

    /**
     * Corpo bruto da resposta; pode conter dados pessoais.
     */
    public String getRawBody() {
        return rawBody;
    }
}
