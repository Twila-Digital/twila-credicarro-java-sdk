package twila.credicarro.v1;

import java.math.BigDecimal;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Simulação de refinanciamento da rota v1 ({@code POST v1/proposal/simulation/refinancing}), sem CNPJ da loja.
 */
@Value
@Builder
public class V1CreateRefinancingSimulationRequest {
    String cpf;
    String licensePlate;
    BigDecimal requestedAmount;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("V1CreateRefinancingSimulationRequest", "cpf", Masking.cpf(cpf), "licensePlate", Masking.mask(licensePlate), "requestedAmount", requestedAmount);
    }
}
