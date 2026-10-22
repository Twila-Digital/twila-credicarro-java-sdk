package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * Participante dono do documento (`participante`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ProposalParticipant implements NamedWireEnum {
    BUYER("COMPRADOR"),
    SELLER("VENDEDOR"),
    STORE("LOJA"),
    UNKNOWN("");

    private final String value;

    ProposalParticipant(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static ProposalParticipant fromWireName(String name) {
        return EnumMapping.fromWireName(ProposalParticipant.class, name);
    }
}
