package twila.credicarro.v1;

import java.math.BigDecimal;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Simulação de financiamento da rota v1 ({@code POST v1/proposal/simulation/financing}), sem CNPJ da loja.
 */
@Value
@Builder
public class V1CreateFinancingSimulationRequest {
    String cpf;
    String licensePlate;
    BigDecimal vehicleAmount;
    BigDecimal downPayment;
    String state;
    boolean isZeroKm;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("V1CreateFinancingSimulationRequest", "cpf", Masking.cpf(cpf), "licensePlate", Masking.mask(licensePlate), "vehicleAmount", vehicleAmount, "downPayment", downPayment, "state", state, "isZeroKm", isZeroKm);
    }
}
