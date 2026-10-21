package twila.credicarro.simulations.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * Tipo da simulação. Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum SimulationType implements NamedWireEnum {
    FINANCING("Financing"),
    REFINANCING("Refinancing"),
    AMOUNT_SIMULATION("AmountSimulation"),
    UNKNOWN("");

    private final String value;

    SimulationType(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static SimulationType fromWireName(String name) {
        return EnumMapping.fromWireName(SimulationType.class, name);
    }
}
