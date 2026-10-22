package twila.credicarro.errors;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import twila.credicarro.internal.http.ApiResponse;
import twila.credicarro.internal.http.RetryAfter;
import twila.credicarro.serialization.CredicarroObjectMapper;

public final class CredicarroExceptionFactory {

    private CredicarroExceptionFactory() {
    }

    public static CredicarroException fromResponse(ApiResponse response) {
        ProblemDetailsModel problem = readProblemDetails(response.body());
        int statusCode = response.statusCode();
        String message = buildMessage(problem, statusCode);

        if (statusCode == 401)
            return new CredicarroAuthenticationException(message, statusCode, problem.getType());

        if (statusCode == 400)
            return new CredicarroValidationException(message, problem, response.body());

        if (statusCode == 422)
            return new CredicarroBusinessRuleException(message, problem, response.body());

        if (statusCode == 429)
            return new CredicarroRateLimitException(message, problem, response.body(), retryAfter(response));

        return new CredicarroApiException(message, statusCode, problem, response.body());
    }

    /**
     * Falha ao gerar o token ({@code 400} ou {@code 401}): sempre {@link CredicarroAuthenticationException}.
     */
    public static CredicarroAuthenticationException authenticationFromResponse(ApiResponse response) {
        ProblemDetailsModel problem = readProblemDetails(response.body());
        return new CredicarroAuthenticationException(buildMessage(problem, response.statusCode()), response.statusCode(), problem.getType());
    }

    /**
     * Mensagem segura para logs: somente status, {@code tipo} e {@code titulo}. O {@code detalhe} pode conter dados pessoais.
     */
    private static String buildMessage(ProblemDetailsModel problem, int statusCode) {
        StringBuilder message = new StringBuilder("A API do Credicarro retornou HTTP ").append(statusCode);

        if (notBlank(problem.getType()))
            message.append(" (").append(problem.getType()).append(")");

        if (notBlank(problem.getTitle()))
            message.append(": ").append(problem.getTitle());
        else if (statusCode == 401 && !notBlank(problem.getType()))
            message.append(": token ausente, inválido ou expirado");

        return message.append('.').toString();
    }

    static ProblemDetailsModel readProblemDetails(String body) {
        if (body == null || body.trim().isEmpty())
            return ProblemDetailsModel.empty();

        try {
            JsonNode root = CredicarroObjectMapper.DEFAULT.readTree(body);
            if (root == null || !root.isObject())
                return ProblemDetailsModel.empty();

            return new ProblemDetailsModel(
                    text(root, "tipo", "type"),
                    text(root, "titulo", "title"),
                    root.path("status").isInt() ? Integer.valueOf(root.path("status").asInt()) : null,
                    text(root, "detalhe", "detail"),
                    text(root, "campo", null),
                    readErrors(root.path("erros")));
        } catch (Exception ex) {
            return ProblemDetailsModel.empty();
        }
    }

    private static List<CredicarroApiError> readErrors(JsonNode node) {
        List<CredicarroApiError> errors = new ArrayList<>();

        if (node == null || !node.isArray())
            return errors;

        for (JsonNode item : node) {
            if (item.isObject())
                errors.add(new CredicarroApiError(text(item, "codigo", null), text(item, "mensagem", null), text(item, "campo", null)));
        }

        return errors;
    }

    private static String text(JsonNode node, String primary, String fallback) {
        String value = textOrNull(node.path(primary));
        if (value == null && fallback != null)
            value = textOrNull(node.path(fallback));

        return value;
    }

    private static String textOrNull(JsonNode node) {
        return node.isTextual() && !node.asText().isEmpty() ? node.asText() : null;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static Duration retryAfter(ApiResponse response) {
        return RetryAfter.parse(response.headers().get("Retry-After"));
    }
}
