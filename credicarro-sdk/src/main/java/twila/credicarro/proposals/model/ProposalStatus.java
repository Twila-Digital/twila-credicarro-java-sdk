package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Status da proposta. Aceita o código ({@code enum_status}) e os nomes em português ({@code EM_ANALISE}, payload v2 e
 * consulta v2) e em inglês ({@code Analysing}, payload v1 e listagem v1). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ProposalStatus implements WireEnum {
    ANALYSING(1, "EM_ANALISE", "Analysing"),
    APPROVED(2, "APROVADA", "Approved"),
    REFUSED(3, "RECUSADA", "Refused"),
    CANCELED(4, "CANCELADA", "Canceled"),
    MANUAL_ANALYSIS(5, "ANALISE_MANUAL", "ManualAnalysis"),
    COMPLETED(6, "FINALIZADA", "Completed"),
    IN_NEGOTIATION(7, "EM_NEGOCIACAO", "InNegotiation"),
    EXPIRED(8, "EXPIRADA", "Expired"),
    PENDING(9, "PENDENTE", "Pending"),
    UNKNOWN(-1, "", "");

    private final int value;
    private final String portugueseName;
    private final String englishName;

    ProposalStatus(int value, String portugueseName, String englishName) {
        this.value = value;
        this.portugueseName = portugueseName;
        this.englishName = englishName;
    }

    @Override
    public int wireValue() {
        return value;
    }

    /**
     * Nome em português ({@code EM_ANALISE}).
     */
    public String portugueseName() {
        return portugueseName;
    }

    /**
     * Nome em inglês ({@code Analysing}).
     */
    public String englishName() {
        return englishName;
    }

    public static ProposalStatus fromWireValue(int value) {
        return EnumMapping.fromWireValue(ProposalStatus.class, value);
    }

    /**
     * Converte o nome (português ou inglês, sem diferenciar maiúsculas).
     */
    public static ProposalStatus fromName(String name) {
        if (name != null) {
            String trimmed = name.trim();
            for (ProposalStatus candidate : values()) {
                if (candidate != UNKNOWN
                        && (candidate.portugueseName.equalsIgnoreCase(trimmed) || candidate.englishName.equalsIgnoreCase(trimmed)))
                    return candidate;
            }
        }

        return UNKNOWN;
    }
}
