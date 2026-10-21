package twila.credicarro.webhooks.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Autenticação enviada pela Credicarro ao chamar a URL (`tipo_autenticacao`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum WebhookAuthenticationType implements WireEnum {
    NONE(1),
    BASIC(2),
    JWT(3),
    UNKNOWN(-1);

    private final int value;

    WebhookAuthenticationType(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static WebhookAuthenticationType fromWireValue(int value) {
        return EnumMapping.fromWireValue(WebhookAuthenticationType.class, value);
    }

    /**
     * Converte o texto devolvido pela listagem (ex.: `Proposal`, `Basic`, `Active`), sem diferenciar maiúsculas.
     */
    public static WebhookAuthenticationType fromName(String name) {
        if (name != null) {
            for (WebhookAuthenticationType candidate : values()) {
                if (candidate != UNKNOWN && candidate.name().equalsIgnoreCase(name.trim()))
                    return candidate;
            }
        }

        return UNKNOWN;
    }
}
