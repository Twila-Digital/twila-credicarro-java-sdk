package twila.credicarro.customers.model;

import java.math.BigDecimal;
import lombok.Builder;
import twila.credicarro.internal.util.Masking;
import lombok.Value;

/**
 * Dados profissionais do cliente (`dadosProfissionais`): ocupação e renda mensal em reais.
 */
@Value
@Builder
public class ProfessionalData {
    OccupationType occupation;
    BigDecimal monthlyIncome;
    /**
     * Dados pessoais, bancários e credenciais aparecem mascarados.
     */
    @Override
    public String toString() {
        return Masking.describe("ProfessionalData", "occupation", occupation, "monthlyIncome", Masking.mask(monthlyIncome));
    }
}
