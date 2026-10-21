package twila.credicarro.serialization;

public final class EnumMapping {

    private EnumMapping() {
    }

    /**
     * Converte o valor inteiro de wire no enum; valores não reconhecidos viram {@code UNKNOWN}.
     */
    public static <E extends Enum<E> & WireEnum> E fromWireValue(Class<E> enumType, int value) {
        for (E candidate : enumType.getEnumConstants()) {
            if (candidate.wireValue() == value)
                return candidate;
        }

        return Enum.valueOf(enumType, "UNKNOWN");
    }

    /**
     * Converte o nome textual de wire (sem diferenciar maiúsculas de minúsculas) no enum; valores ausentes ou não
     * reconhecidos viram {@code UNKNOWN}.
     */
    public static <E extends Enum<E> & NamedWireEnum> E fromWireName(Class<E> enumType, String name) {
        if (name != null) {
            for (E candidate : enumType.getEnumConstants()) {
                if (name.equalsIgnoreCase(candidate.wireName()))
                    return candidate;
            }
        }

        return Enum.valueOf(enumType, "UNKNOWN");
    }
}
