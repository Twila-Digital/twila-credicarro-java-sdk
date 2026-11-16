package twila.credicarro.simulations.model;

import java.math.BigDecimal;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Simulação de financiamento ({@code POST v2/proposal/simulation/financing}). A loja é selecionada pelo CNPJ.
 */
@Value
@Builder
public class CreateFinancingSimulationRequest {
    String cpf;
    String licensePlate;
    BigDecimal vehicleAmount;
    BigDecimal downPayment;
    String state;
    boolean isZeroKm;
    String storeCnpj;
    /**
     * Retorno de comissão ({@code retornoComissao}), opcional. Quando informado, precisa estar na faixa
     * {@code Financing} da loja (veja {@code SimulationsClient.getCommissionReturns}); {@code null} não é enviado.
     */
    Integer commissionReturn;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("CreateFinancingSimulationRequest", "cpf", Masking.cpf(cpf), "licensePlate", Masking.mask(licensePlate), "vehicleAmount", vehicleAmount, "downPayment", downPayment, "state", state, "isZeroKm", isZeroKm, "storeCnpj", storeCnpj, "commissionReturn", commissionReturn);
    }
}
