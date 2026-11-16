package twila.credicarro.v1;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import twila.credicarro.internal.util.Masking;

/**
 * Simulação de financiamento sem CPF ({@code POST v1/proposal/simulation/financing/without-cpf}). {@code downPayment}
 * é opcional: omitido, a API usa a entrada mínima da taxa da loja.
 */
@Value
@Builder
public class CreateFinancingWithoutCpfRequest {
    String licensePlate;
    BigDecimal vehicleAmount;
    BigDecimal downPayment;
    String state;
    /**
     * A placa aparece mascarada.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateFinancingWithoutCpfRequest", "licensePlate", Masking.mask(licensePlate), "vehicleAmount", vehicleAmount, "downPayment", downPayment, "state", state);
    }
}
