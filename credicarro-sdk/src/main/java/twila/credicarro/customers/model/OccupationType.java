package twila.credicarro.customers.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Tipo de ocupação (`tipoOcupacao`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum OccupationType implements WireEnum {
    RETIRED(1),
    PENSIONER(2),
    EMPLOYEE(3),
    SELF_EMPLOYED(4),
    FREELANCER(6),
    BUSINESS_OWNER(7),
    UNKNOWN(-1);

    private final int value;

    OccupationType(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static OccupationType fromWireValue(int value) {
        return EnumMapping.fromWireValue(OccupationType.class, value);
    }
}
