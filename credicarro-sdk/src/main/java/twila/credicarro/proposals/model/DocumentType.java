package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Tipo de documento (`tipoDocumento` / `enum_tipo`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum DocumentType implements WireEnum {
    CNH(1),
    CPF(2),
    RG(3),
    PROOF_OF_INCOME(4),
    PROOF_OF_ADDRESS(5),
    VEHICLE_DOCUMENT(6),
    DIGITAL_CNH(7),
    ECV_REPORT(8),
    INSPECTION_REPORT(9),
    CNPJ_CARD(11),
    ATPV(57),
    UNKNOWN(-1);

    private final int value;

    DocumentType(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static DocumentType fromWireValue(int value) {
        return EnumMapping.fromWireValue(DocumentType.class, value);
    }
}
