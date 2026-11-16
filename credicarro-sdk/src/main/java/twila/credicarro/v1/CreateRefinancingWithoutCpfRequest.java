package twila.credicarro.v1;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Simulação de refinanciamento sem CPF ({@code POST v1/proposal/simulation/refinancing/without-cpf}). Todos os campos
 * são obrigatórios; {@code state} (UF) define as taxas e o custo de registro.
 */
@Value
@Builder
public class CreateRefinancingWithoutCpfRequest {
    String licensePlate;
    BigDecimal requestedAmount;
    String state;
    /**
     * A placa aparece mascarada.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateRefinancingWithoutCpfRequest", "licensePlate", Masking.mask(licensePlate), "requestedAmount", requestedAmount, "state", state);
    }
}
