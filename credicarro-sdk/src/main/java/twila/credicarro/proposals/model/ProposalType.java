package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * Modalidade da proposta. Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ProposalType implements NamedWireEnum {
    FINANCING("Financing"),
    REFINANCING("Refinancing"),
    UNKNOWN("");

    private final String value;

    ProposalType(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static ProposalType fromWireName(String name) {
        return EnumMapping.fromWireName(ProposalType.class, name);
    }
}
