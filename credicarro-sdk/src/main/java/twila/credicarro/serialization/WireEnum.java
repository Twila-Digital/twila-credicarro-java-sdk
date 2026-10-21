package twila.credicarro.serialization;

/**
 * Enum com valor inteiro de wire. Requer um membro {@code UNKNOWN} (ver {@link EnumMapping#fromWireValue}).
 */
public interface WireEnum {

    int wireValue();
}
