package twila.credicarro.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import twila.credicarro.customers.model.OccupationType;
import twila.credicarro.proposals.model.ActionOwner;
import twila.credicarro.proposals.model.BiometryStatus;
import twila.credicarro.proposals.model.DepositAccountType;
import twila.credicarro.proposals.model.DocumentStatus;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalParticipant;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalStep;
import twila.credicarro.proposals.model.ProposalType;
import twila.credicarro.proposals.model.StepSituation;
import twila.credicarro.simulations.model.SimulationType;
import twila.credicarro.webhooks.model.ProposalEventReason;
import twila.credicarro.webhooks.model.WebhookAuthenticationType;
import twila.credicarro.webhooks.model.WebhookStatus;
import twila.credicarro.webhooks.model.WebhookType;

class EnumMappingTest {

    @Test
    void mapsKnownWireValues() {
        assertThat(ProposalStatus.fromWireValue(6)).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(ProposalStep.fromWireValue(8)).isEqualTo(ProposalStep.VEHICLE_DETAILS);
        assertThat(StepSituation.fromWireValue(4)).isEqualTo(StepSituation.PENDING_CORRECTION);
        assertThat(ActionOwner.fromWireValue(2)).isEqualTo(ActionOwner.INTEGRATOR);
        assertThat(BiometryStatus.fromWireValue(3)).isEqualTo(BiometryStatus.APPROVED);
        assertThat(DocumentStatus.fromWireValue(5)).isEqualTo(DocumentStatus.PENDING);
        assertThat(DocumentType.fromWireValue(57)).isEqualTo(DocumentType.ATPV);
        assertThat(OccupationType.fromWireValue(6)).isEqualTo(OccupationType.FREELANCER);
        assertThat(WebhookType.fromWireValue(3)).isEqualTo(WebhookType.PROPOSAL);
        assertThat(WebhookAuthenticationType.fromWireValue(3)).isEqualTo(WebhookAuthenticationType.JWT);
        assertThat(WebhookStatus.fromWireValue(2)).isEqualTo(WebhookStatus.INACTIVE);
    }

    @Test
    void everyIntegerEnumFallsBackToUnknownForUnrecognizedValues() {
        assertThat(ProposalStatus.fromWireValue(9999)).isEqualTo(ProposalStatus.UNKNOWN);
        assertThat(ProposalStep.fromWireValue(-42)).isEqualTo(ProposalStep.UNKNOWN);
        assertThat(StepSituation.fromWireValue(99)).isEqualTo(StepSituation.UNKNOWN);
        assertThat(ActionOwner.fromWireValue(99)).isEqualTo(ActionOwner.UNKNOWN);
        assertThat(BiometryStatus.fromWireValue(99)).isEqualTo(BiometryStatus.UNKNOWN);
        assertThat(DocumentStatus.fromWireValue(99)).isEqualTo(DocumentStatus.UNKNOWN);
        assertThat(DocumentType.fromWireValue(10)).isEqualTo(DocumentType.UNKNOWN);
        assertThat(OccupationType.fromWireValue(5)).isEqualTo(OccupationType.UNKNOWN);
        assertThat(WebhookType.fromWireValue(0)).isEqualTo(WebhookType.UNKNOWN);
        assertThat(WebhookAuthenticationType.fromWireValue(0)).isEqualTo(WebhookAuthenticationType.UNKNOWN);
        assertThat(WebhookStatus.fromWireValue(0)).isEqualTo(WebhookStatus.UNKNOWN);
    }

    @Test
    void unknownIsMinusOneAndWireValuesRoundTrip() {
        assertThat(ProposalStatus.UNKNOWN.wireValue()).isEqualTo(-1);

        for (ProposalStep step : ProposalStep.values()) {
            if (step != ProposalStep.UNKNOWN)
                assertThat(ProposalStep.fromWireValue(step.wireValue())).isEqualTo(step);
        }

        for (DocumentType type : DocumentType.values()) {
            if (type != DocumentType.UNKNOWN)
                assertThat(DocumentType.fromWireValue(type.wireValue())).isEqualTo(type);
        }
    }

    @Test
    void proposalStatusAcceptsPortugueseAndEnglishNames() {
        assertThat(ProposalStatus.fromName("EM_ANALISE")).isEqualTo(ProposalStatus.ANALYSING);
        assertThat(ProposalStatus.fromName("Analysing")).isEqualTo(ProposalStatus.ANALYSING);
        assertThat(ProposalStatus.fromName("aprovada")).isEqualTo(ProposalStatus.APPROVED);
        assertThat(ProposalStatus.fromName("ManualAnalysis")).isEqualTo(ProposalStatus.MANUAL_ANALYSIS);
        assertThat(ProposalStatus.fromName("FINALIZADA")).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(ProposalStatus.fromName("Completed")).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(ProposalStatus.fromName("algo-novo")).isEqualTo(ProposalStatus.UNKNOWN);
        assertThat(ProposalStatus.fromName(null)).isEqualTo(ProposalStatus.UNKNOWN);
    }

    @Test
    void textualEnumsMapTheirWireNamesAndFallBackToUnknown() {
        assertThat(ProposalType.fromWireName("Financing")).isEqualTo(ProposalType.FINANCING);
        assertThat(ProposalType.fromWireName("Leasing")).isEqualTo(ProposalType.UNKNOWN);
        assertThat(SimulationType.fromWireName("AmountSimulation")).isEqualTo(SimulationType.AMOUNT_SIMULATION);
        assertThat(DepositAccountType.PAYMENT.wireName()).isEqualTo("PAGAMENTO");
        assertThat(DepositAccountType.CHECKING.wireName()).isEqualTo("CORRENTE");
        assertThat(DepositAccountType.SAVINGS.wireName()).isEqualTo("POUPANCA");
        assertThat(ProposalParticipant.BUYER.wireName()).isEqualTo("COMPRADOR");
        assertThat(ProposalParticipant.SELLER.wireName()).isEqualTo("VENDEDOR");
        assertThat(ProposalParticipant.STORE.wireName()).isEqualTo("LOJA");
        assertThat(ProposalEventReason.fromWireName("ETAPAS_ALTERADAS")).isEqualTo(ProposalEventReason.STEPS_CHANGED);
        assertThat(ProposalEventReason.fromWireName("NOVO")).isEqualTo(ProposalEventReason.UNKNOWN);
        assertThat(ProposalEventReason.fromWireName(null)).isEqualTo(ProposalEventReason.UNKNOWN);
    }

    @Test
    void webhookEnumsParseTheTextReturnedByTheListing() {
        assertThat(WebhookType.fromName("Proposal")).isEqualTo(WebhookType.PROPOSAL);
        assertThat(WebhookAuthenticationType.fromName("Basic")).isEqualTo(WebhookAuthenticationType.BASIC);
        assertThat(WebhookAuthenticationType.fromName("Jwt")).isEqualTo(WebhookAuthenticationType.JWT);
        assertThat(WebhookStatus.fromName("Active")).isEqualTo(WebhookStatus.ACTIVE);
        assertThat(WebhookStatus.fromName("Suspenso")).isEqualTo(WebhookStatus.UNKNOWN);
        assertThat(WebhookType.fromName(null)).isEqualTo(WebhookType.UNKNOWN);
    }
}
