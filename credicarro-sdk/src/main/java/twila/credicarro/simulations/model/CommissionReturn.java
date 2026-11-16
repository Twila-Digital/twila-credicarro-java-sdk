package twila.credicarro.simulations.model;

import lombok.Value;

/**
 * Faixa de retorno de comissão configurada para a loja. A simulação valida pela faixa com
 * {@link CommissionReturnTarget#STORE}.
 */
@Value
public class CommissionReturn {
    /** Modalidade da faixa ({@code tipo}): {@link SimulationType#FINANCING} ou {@link SimulationType#REFINANCING}. */
    SimulationType type;
    /** A quem a faixa se aplica ({@code alvo}). */
    CommissionReturnTarget target;
    /** Menor {@code commissionReturn} aceito ({@code retorno_minimo}). */
    int minimumReturn;
    /** Maior {@code commissionReturn} aceito ({@code retorno_maximo}). */
    int maximumReturn;
    /** Indica se a loja permite usar a taxa padrão ({@code permite_taxa_padrao}). */
    boolean allowsDefaultRate;
}
