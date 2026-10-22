package twila.credicarro.serialization;

/**
 * Enum com valor textual de wire (ex.: {@code COMPRADOR}, {@code Financing}). Requer um membro {@code UNKNOWN}
 * (ver {@link EnumMapping#fromWireName}).
 */
public interface NamedWireEnum {

    String wireName();
}
