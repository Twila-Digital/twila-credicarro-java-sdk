package twila.credicarro.simulations.model;

import lombok.Value;

/**
 * Faixa de retorno de comissão configurada para a loja, por tipo de simulação
 * ({@code GET v2/proposal/simulation/commission-returns}).
 */
@Value
public class CommissionReturn {
    CommissionReturnType type;
    CommissionReturnTarget target;
    int minimumReturn;
    int maximumReturn;
    /**
     * Indica se a loja aceita simular sem informar {@code commissionReturn}.
     */
    boolean allowDefaultRate;
}
