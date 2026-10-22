package twila.credicarro.simulations.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * Tipo de simulação ao qual o retorno de comissão se aplica. Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum CommissionReturnType implements NamedWireEnum {
    FINANCING("Financing"),
    REFINANCING("Refinancing"),
    UNKNOWN("");

    private final String value;

    CommissionReturnType(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static CommissionReturnType fromWireName(String name) {
        return EnumMapping.fromWireName(CommissionReturnType.class, name);
    }
}
