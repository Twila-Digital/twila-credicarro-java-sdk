package twila.credicarro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroAuthenticationException;
import twila.credicarro.errors.CredicarroTimeoutException;
import twila.credicarro.support.ApiTestSupport;
import twila.credicarro.support.Fixtures;

class AuthenticationWireMockTest extends ApiTestSupport {

    private final UUID proposalId = UUID.fromString(Fixtures.PROPOSAL_ID);
    private final String proposalUrl = path("v2/proposal/" + Fixtures.PROPOSAL_ID);

    @Test
    void requestsTheTokenWithClientCredentialsAndSendsItAsBearer() {
        server.stubFor(get(urlEqualTo(proposalUrl)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        client.proposals().get(proposalId);

        server.verify(postRequestedFor(urlEqualTo(TOKEN_PATH))
                .withRequestBody(containing("\"clientId\":\"client-id\""))
                .withRequestBody(containing("\"clientSecret\":\"" + CLIENT_SECRET + "\"")));
        server.verify(getRequestedFor(urlEqualTo(proposalUrl)).withHeader("Authorization", equalTo("Bearer token-de-teste")));
    }

    @Test
    void cachesTheTokenAcrossCalls() {
        server.stubFor(get(urlEqualTo(proposalUrl)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        client.proposals().get(proposalId);
        client.proposals().get(proposalId);
        client.proposals().get(proposalId);

        server.verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        server.verify(3, getRequestedFor(urlEqualTo(proposalUrl)));
    }

    @Test
    void usesExpiraEmToDecideWhenToRenew() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(jsonResponse(200,
                "{'token_de_acesso':'token-expirado','tipo_de_token':'Bearer','expira_em_segundos':3600,"
                        + "'expira_em':'2020-01-01T00:00:00+00:00','escopo':'x'}")));
        server.stubFor(get(urlEqualTo(proposalUrl)).willReturn(jsonResponse(200, Fixtures.proposalV2())));

        client.proposals().get(proposalId);
        client.proposals().get(proposalId);

        server.verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void refreshesTheTokenOnceOn401AndRetriesTheCall() {
        server.stubFor(get(urlEqualTo(proposalUrl)).inScenario("auth")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(401))
                .willSetStateTo("renovado"));
        server.stubFor(get(urlEqualTo(proposalUrl)).inScenario("auth")
                .whenScenarioStateIs("renovado")
                .willReturn(jsonResponse(200, Fixtures.proposalV2())));

        assertThat(client.proposals().get(proposalId).getId()).isEqualTo(proposalId);

        server.verify(2, getRequestedFor(urlEqualTo(proposalUrl)));
        server.verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void persistent401WithoutBodyBecomesAnAuthenticationException() {
        server.stubFor(get(urlEqualTo(proposalUrl)).willReturn(aResponse().withStatus(401)));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroAuthenticationException.class)
                .satisfies(ex -> {
                    CredicarroAuthenticationException auth = (CredicarroAuthenticationException) ex;
                    assertThat(auth.getStatusCode()).isEqualTo(401);
                    assertThat(auth.getMessage()).contains("401").doesNotContain(CLIENT_SECRET).doesNotContain("token-de-teste");
                });

        server.verify(2, getRequestedFor(urlEqualTo(proposalUrl)));
        server.verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }

    @Test
    void invalidCredentialsBecomeAnAuthenticationExceptionWithoutLeakingTheSecret() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(jsonResponse(401,
                "{'tipo':'Integration.Authentication.InvalidCredentials','titulo':'Não autorizado','status':401,'detalhe':'Credenciais inválidas.'}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroAuthenticationException.class)
                .satisfies(ex -> {
                    CredicarroAuthenticationException auth = (CredicarroAuthenticationException) ex;
                    assertThat(auth.getErrorCode()).isEqualTo("Integration.Authentication.InvalidCredentials");
                    assertThat(auth.getMessage()).contains("Integration.Authentication.InvalidCredentials").doesNotContain(CLIENT_SECRET);
                });
    }

    @Test
    void badTokenRequestBecomesAnAuthenticationException() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(jsonResponse(400,
                "{'tipo':'Validation.Field.Required','titulo':'Requisição inválida','status':400,'campo':'clientSecret'}")));

        assertThatThrownBy(() -> client.proposals().get(proposalId))
                .isInstanceOf(CredicarroAuthenticationException.class)
                .satisfies(ex -> assertThat(((CredicarroAuthenticationException) ex).getStatusCode()).isEqualTo(400));
    }

    @Test
    void tokenNetworkFailureIsRetriedAndThenReportedAsTimeoutOrNetworkError() {
        server.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> client.proposals().get(proposalId)).isInstanceOf(CredicarroTimeoutException.class);

        server.verify(3, postRequestedFor(urlEqualTo(TOKEN_PATH)));
    }
}
