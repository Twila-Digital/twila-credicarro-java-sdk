package twila.credicarro.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import twila.credicarro.internal.http.ApiResponse;

class CredicarroExceptionFactoryTest {

    @Test
    void mapsUnprocessableEntityToBusinessRuleException() {
        ApiResponse response = responseOf(422,
                "{\"tipo\":\"Store.Integration.Intermediation\",\"titulo\":\"Regra de negócio não atendida\",\"status\":422,"
                        + "\"detalhe\":\"A loja possui contrato de intermediação e não é elegível para a integração.\"}");

        CredicarroException exception = CredicarroExceptionFactory.fromResponse(response);

        assertThat(exception).isInstanceOf(CredicarroBusinessRuleException.class);
        CredicarroApiException api = (CredicarroApiException) exception;
        assertThat(api.getStatusCode()).isEqualTo(422);
        assertThat(api.getErrorCode()).isEqualTo("Store.Integration.Intermediation");
        assertThat(api.getTitle()).isEqualTo("Regra de negócio não atendida");
        assertThat(api.getDetail()).contains("intermediação");
        assertThat(api.getField()).isNull();
    }

    @Test
    void mapsBadRequestToValidationExceptionWithFieldAndErrorsList() {
        ApiResponse response = responseOf(400,
                "{\"tipo\":\"Validation.InvalidFormat\",\"titulo\":\"Requisição inválida\",\"status\":400,\"detalhe\":\"JSON inválido\","
                        + "\"campo\":\"valorVeiculo\",\"erros\":[{\"codigo\":\"Validation.InvalidFormat\","
                        + "\"mensagem\":\"O valor informado para 'valorVeiculo' não é um número válido.\",\"campo\":\"valorVeiculo\"}]}");

        CredicarroApiException exception = (CredicarroApiException) CredicarroExceptionFactory.fromResponse(response);

        assertThat(exception).isInstanceOf(CredicarroValidationException.class);
        assertThat(exception.getField()).isEqualTo("valorVeiculo");
        assertThat(exception.getErrors()).hasSize(1);
        assertThat(exception.getErrors().get(0).getCode()).isEqualTo("Validation.InvalidFormat");
        assertThat(exception.getErrors().get(0).getField()).isEqualTo("valorVeiculo");
        assertThat(exception.getRawBody()).contains("valorVeiculo");
    }

    @Test
    void fallsBackToTypeTitleDetailOfStandardProblemDetails() {
        ApiResponse response = responseOf(500, "{\"type\":\"about:blank\",\"title\":\"Erro inesperado\",\"status\":500,\"detail\":\"falha interna\"}");

        CredicarroApiException exception = (CredicarroApiException) CredicarroExceptionFactory.fromResponse(response);

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getErrorCode()).isEqualTo("about:blank");
        assertThat(exception.getTitle()).isEqualTo("Erro inesperado");
        assertThat(exception.getDetail()).isEqualTo("falha interna");
    }

    @Test
    void unauthorizedWithoutBodyBecomesAnAuthenticationException() {
        CredicarroException exception = CredicarroExceptionFactory.fromResponse(responseOf(401, ""));

        assertThat(exception).isInstanceOf(CredicarroAuthenticationException.class);
        assertThat(((CredicarroAuthenticationException) exception).getStatusCode()).isEqualTo(401);
        assertThat(exception.getMessage()).contains("401");
    }

    @Test
    void mapsTooManyRequestsToRateLimitExceptionWithRetryAfter() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Retry-After", "30");

        CredicarroException exception = CredicarroExceptionFactory.fromResponse(responseOf(429, "{\"tipo\":\"RateLimit\"}", headers));

        assertThat(exception).isInstanceOf(CredicarroRateLimitException.class);
        assertThat(((CredicarroRateLimitException) exception).getRetryAfter()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void otherStatusesBecomeGenericApiExceptions() {
        for (int status : new int[] {403, 404, 409, 502, 503}) {
            CredicarroException exception = CredicarroExceptionFactory.fromResponse(responseOf(status, "{\"tipo\":\"X.Y.Z\"}"));

            assertThat(exception).isInstanceOf(CredicarroApiException.class);
            assertThat(exception).isNotInstanceOfAny(CredicarroValidationException.class, CredicarroBusinessRuleException.class,
                    CredicarroRateLimitException.class);
            assertThat(((CredicarroApiException) exception).getStatusCode()).isEqualTo(status);
        }
    }

    @Test
    void messageHasOnlyStatusTypeAndTitleNeverTheDetailOrTheBody() {
        ApiResponse response = responseOf(422,
                "{\"tipo\":\"Customer.Account.Incomplete\",\"titulo\":\"Regra de negócio não atendida\","
                        + "\"detalhe\":\"O cadastro de 123.456.789-09 está incompleto.\"}");

        CredicarroException exception = CredicarroExceptionFactory.fromResponse(response);

        assertThat(exception.getMessage())
                .contains("422")
                .contains("Customer.Account.Incomplete")
                .contains("Regra de negócio não atendida")
                .doesNotContain("123.456.789-09");
    }

    @Test
    void toleratesEmptyOrInvalidBodies() {
        for (String body : new String[] {"", "não é json", "[1,2]", "null", "{\"erros\":\"texto\"}"}) {
            CredicarroException exception = CredicarroExceptionFactory.fromResponse(responseOf(500, body));

            assertThat(exception).isInstanceOf(CredicarroApiException.class);
            assertThat(((CredicarroApiException) exception).getErrors()).isEmpty();
            assertThat(exception.getMessage()).contains("500");
        }
    }

    @Test
    void authenticationFailuresFromTheTokenRouteKeepStatusAndType() {
        CredicarroAuthenticationException exception = CredicarroExceptionFactory.authenticationFromResponse(responseOf(400,
                "{\"tipo\":\"Validation.Field.Required\",\"titulo\":\"Requisição inválida\",\"campo\":\"clientSecret\"}"));

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getErrorCode()).isEqualTo("Validation.Field.Required");
    }

    private static ApiResponse responseOf(int statusCode, String body) {
        return responseOf(statusCode, body, Collections.<String, String>emptyMap());
    }

    private static ApiResponse responseOf(int statusCode, String body, Map<String, String> headers) {
        Headers.Builder headersBuilder = new Headers.Builder();
        for (Map.Entry<String, String> entry : headers.entrySet())
            headersBuilder.add(entry.getKey(), entry.getValue());

        return new ApiResponse(statusCode, body, headersBuilder.build());
    }
}
