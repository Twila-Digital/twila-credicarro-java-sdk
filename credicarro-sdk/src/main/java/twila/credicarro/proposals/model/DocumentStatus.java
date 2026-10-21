package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Status do documento (`enum_status` em `documentos`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum DocumentStatus implements WireEnum {
    RECEIVED(0),
    VALID(1),
    INVALID(2),
    IN_ANALYSIS(3),
    DELETED(4),
    PENDING(5),
    UNKNOWN(-1);

    private final int value;

    DocumentStatus(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static DocumentStatus fromWireValue(int value) {
        return EnumMapping.fromWireValue(DocumentStatus.class, value);
    }
}
