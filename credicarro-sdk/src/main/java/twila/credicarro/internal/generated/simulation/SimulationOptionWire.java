package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Opcao de simulacao (item de simulacoes).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SimulationOptionWire {

    @JsonProperty("id_simulacao")
    public UUID idSimulacao;

    @JsonProperty("valor_solicitado")
    public BigDecimal valorSolicitado;

    @JsonProperty("prazo")
    public Integer prazo;

    @JsonProperty("valor_parcela")
    public BigDecimal valorParcela;

    @JsonProperty("valor_tac")
    public BigDecimal valorTac;

    @JsonProperty("valor_boleto")
    public BigDecimal valorBoleto;

    @JsonProperty("valor_seguro")
    public BigDecimal valorSeguro;

    @JsonProperty("valor_iof")
    public BigDecimal valorIof;

    @JsonProperty("percentual_iof")
    public BigDecimal percentualIof;

    @JsonProperty("valor_credito_total")
    public BigDecimal valorCreditoTotal;

    @JsonProperty("valor_divida_total")
    public BigDecimal valorDividaTotal;

    @JsonProperty("percentual_cet_mensal")
    public BigDecimal percentualCetMensal;

    @JsonProperty("percentual_cet_anual")
    public BigDecimal percentualCetAnual;

    @JsonProperty("percentual_juros_mensal")
    public BigDecimal percentualJurosMensal;

    @JsonProperty("percentual_juros_anual")
    public BigDecimal percentualJurosAnual;

    @JsonProperty("valor_retencao_diaria_aproximado")
    public BigDecimal valorRetencaoDiariaAproximado;
}
