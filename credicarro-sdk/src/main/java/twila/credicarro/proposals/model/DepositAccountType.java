package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * Tipo da conta de recebimento (`tipoConta`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum DepositAccountType implements NamedWireEnum {
    CHECKING("CORRENTE"),
    SAVINGS("POUPANCA"),
    PAYMENT("PAGAMENTO"),
    UNKNOWN("");

    private final String value;

    DepositAccountType(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static DepositAccountType fromWireName(String name) {
        return EnumMapping.fromWireName(DepositAccountType.class, name);
    }
}
