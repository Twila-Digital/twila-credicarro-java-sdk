package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Situação da biometria (`enum_biometria`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum BiometryStatus implements WireEnum {
    NOT_STARTED(0),
    SESSION_CREATED(1),
    IN_ANALYSIS(2),
    APPROVED(3),
    REJECTED(4),
    UNKNOWN(-1);

    private final int value;

    BiometryStatus(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static BiometryStatus fromWireValue(int value) {
        return EnumMapping.fromWireValue(BiometryStatus.class, value);
    }
}
