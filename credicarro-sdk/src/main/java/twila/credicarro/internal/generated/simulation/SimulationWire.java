package twila.credicarro.internal.generated.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Schema SimulationResponse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SimulationWire {

    @JsonProperty("id_simulacao")
    public UUID idSimulacao;

    @JsonProperty("prazo")
    public Long prazo;

    @JsonProperty("valor_parcela")
    public BigDecimal valorParcela;

    @JsonProperty("valor_solicitado")
    public BigDecimal valorSolicitado;

    @JsonProperty("valor_entrada")
    public BigDecimal valorEntrada;

    @JsonProperty("valor_bem")
    public BigDecimal valorBem;

    @JsonProperty("valor_tac")
    public BigDecimal valorTac;

    @JsonProperty("taxa_juros_negociada")
    public BigDecimal taxaJurosNegociada;

    @JsonProperty("valor_registro")
    public BigDecimal valorRegistro;

    @JsonProperty("uf")
    public String uf;

    @JsonProperty("tipo")
    public String tipo;

    @JsonProperty("transacao")
    public TransactionWire transacao;

    @JsonProperty("juros")
    public InterestWire juros;

    @JsonProperty("cet")
    public CetWire cet;

    @JsonProperty("iof")
    public IofWire iof;

    @JsonProperty("veiculo")
    public VehicleWire veiculo;

    @JsonProperty("cliente")
    public CustomerRefWire cliente;

    @JsonProperty("data_criacao")
    public OffsetDateTime dataCriacao;
}
