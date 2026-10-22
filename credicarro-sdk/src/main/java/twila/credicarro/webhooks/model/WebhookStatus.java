package twila.credicarro.webhooks.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Status do webhook. Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum WebhookStatus implements WireEnum {
    ACTIVE(1),
    INACTIVE(2),
    UNKNOWN(-1);

    private final int value;

    WebhookStatus(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static WebhookStatus fromWireValue(int value) {
        return EnumMapping.fromWireValue(WebhookStatus.class, value);
    }

    /**
     * Converte o texto devolvido pela listagem (ex.: `Proposal`, `Basic`, `Active`), sem diferenciar maiúsculas.
     */
    public static WebhookStatus fromName(String name) {
        if (name != null) {
            for (WebhookStatus candidate : values()) {
                if (candidate != UNKNOWN && candidate.name().equalsIgnoreCase(name.trim()))
                    return candidate;
            }
        }

        return UNKNOWN;
    }
}
