package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Situação da etapa (`enum_situacao`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum StepSituation implements WireEnum {
    AWAITING_SUBMISSION(1),
    AWAITING_CUSTOMER(2),
    PROCESSING(3),
    PENDING_CORRECTION(4),
    BLOCKED(5),
    COMPLETED(6),
    CANCELED(7),
    UNKNOWN(-1);

    private final int value;

    StepSituation(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static StepSituation fromWireValue(int value) {
        return EnumMapping.fromWireValue(StepSituation.class, value);
    }
}
