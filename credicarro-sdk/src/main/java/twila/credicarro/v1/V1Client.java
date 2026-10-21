package twila.credicarro.v1;

/**
 * Rotas v1 que não selecionam a loja pelo CNPJ ({@code v1/proposal/simulation/*} e {@code v1/proposal}). Para
 * novas integrações prefira as rotas v2 de {@code client.simulations()} e {@code client.proposals()}.
 */
public interface V1Client {

    V1SimulationsClient simulations();

    V1ProposalsClient proposals();
}
