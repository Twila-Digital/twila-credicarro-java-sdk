package twila.credicarro.simulations.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * A quem pertence a configuração de comissão. Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum CommissionReturnTarget implements NamedWireEnum {
    STORE("Store"),
    ORGANIZATION("Organization"),
    UNKNOWN("");

    private final String value;

    CommissionReturnTarget(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static CommissionReturnTarget fromWireName(String name) {
        return EnumMapping.fromWireName(CommissionReturnTarget.class, name);
    }
}
