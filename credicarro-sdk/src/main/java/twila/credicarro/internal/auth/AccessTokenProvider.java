package twila.credicarro.internal.auth;

import twila.credicarro.internal.resilience.Deadline;

public interface AccessTokenProvider {

    /**
     * Devolve o token em cache enquanto válido; senão gera um novo (uma única requisição de token por vez), dentro do
     * orçamento de tempo da chamada.
     */
    String getToken(Deadline deadline);

    /**
     * Descarta o token em cache somente se {@code rejectedToken} ainda for o token atual — se outra thread já o
     * renovou, o token novo é preservado.
     */
    void invalidate(String rejectedToken);
}
