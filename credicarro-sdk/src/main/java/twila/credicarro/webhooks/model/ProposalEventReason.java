package twila.credicarro.webhooks.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.NamedWireEnum;

/**
 * O que disparou a notificação do webhook v2 (`motivo_evento`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ProposalEventReason implements NamedWireEnum {
    STATUS_CHANGED("STATUS_ALTERADO"),
    STEPS_CHANGED("ETAPAS_ALTERADAS"),
    DOCUMENTS_CHANGED("DOCUMENTOS_ALTERADOS"),
    UNKNOWN("");

    private final String value;

    ProposalEventReason(String value) {
        this.value = value;
    }

    @Override
    public String wireName() {
        return value;
    }

    public static ProposalEventReason fromWireName(String name) {
        return EnumMapping.fromWireName(ProposalEventReason.class, name);
    }
}
