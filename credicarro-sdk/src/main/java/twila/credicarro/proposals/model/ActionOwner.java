package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Responsável pela ação (`enum_responsavel_acao`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ActionOwner implements WireEnum {
    NONE(0),
    CUSTOMER(1),
    INTEGRATOR(2),
    CREDICARRO(3),
    UNKNOWN(-1);

    private final int value;

    ActionOwner(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static ActionOwner fromWireValue(int value) {
        return EnumMapping.fromWireValue(ActionOwner.class, value);
    }
}
