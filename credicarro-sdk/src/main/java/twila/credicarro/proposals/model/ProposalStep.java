package twila.credicarro.proposals.model;

import twila.credicarro.serialization.EnumMapping;
import twila.credicarro.serialization.WireEnum;

/**
 * Etapa da proposta (`enum_etapa`). Valores não reconhecidos viram {@link #UNKNOWN}.
 */
public enum ProposalStep implements WireEnum {
    UNDEFINED(0),
    PROPOSAL_CREATED(1),
    PROPOSAL_APPROVED(2),
    SELLER_NOTIFICATION(3),
    BIOMETRY_ANALYSIS(4),
    DEPOSIT_ACCOUNT(5),
    DOCUMENT_SUBMISSION(6),
    DOCUMENT_CORRECTION(7),
    VEHICLE_DETAILS(8),
    INSPECTION(9),
    SIGNATURE(10),
    PAYMENT(11),
    PROPOSAL_COMPLETED(12),
    UNKNOWN(-1);

    private final int value;

    ProposalStep(int value) {
        this.value = value;
    }

    @Override
    public int wireValue() {
        return value;
    }

    public static ProposalStep fromWireValue(int value) {
        return EnumMapping.fromWireValue(ProposalStep.class, value);
    }
}
