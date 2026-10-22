package twila.credicarro.webhooks.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Evento notificado pelo webhook (`tipo`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum WebhookType implements WireEnum {
    CUSTOMER(1),
    SIMULATION(2),
    PROPOSAL(3),
    UNKNOWN(-1);

    private final int value;

    WebhookType(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static WebhookType fromWireValue(int value) {
        return EnumMapping.fromWireValue(WebhookType.class, value);
    }

    /**
     * Converte o texto devolvido pela listagem (ex.: `Proposal`, `Basic`, `Active`), sem diferenciar maiúsculas.
     */
    public static WebhookType fromName(String name) {
        if (name != null) {
            for (WebhookType candidate : values()) {
                if (candidate != UNKNOWN && candidate.name().equalsIgnoreCase(name.trim()))
                    return candidate;
            }
        }

        return UNKNOWN;
    }
}
