package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Simulação de refinanciamento ({@code POST v2/proposal/simulation/refinancing}). A loja é selecionada pelo CNPJ.
 */
@Value
@Builder
public class CreateRefinancingSimulationRequest {
    String cpf;
    String licensePlate;
    BigDecimal requestedAmount;
    String storeCnpj;
    /**
     * Retorno de comissão a aplicar, dentro da faixa configurada na loja. Opcional: {@code null} usa a taxa padrão,
     * quando a loja permitir. Consulte a faixa aceita com {@link twila.credicarro.simulations.SimulationsClient#getCommissionReturns}.
     */
    Integer commissionReturn;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateRefinancingSimulationRequest", "cpf", Masking.cpf(cpf), "licensePlate", Masking.mask(licensePlate), "requestedAmount", requestedAmount, "storeCnpj", storeCnpj, "commissionReturn", commissionReturn);
    }
}
