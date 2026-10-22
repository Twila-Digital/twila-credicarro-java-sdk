package twila.credicarro.webhooks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import twila.credicarro.errors.CredicarroWebhookSignatureException;
import twila.credicarro.proposals.model.ActionOwner;
import twila.credicarro.proposals.model.DocumentStatus;
import twila.credicarro.proposals.model.DocumentType;
import twila.credicarro.proposals.model.ProposalStatus;
import twila.credicarro.proposals.model.ProposalStep;
import twila.credicarro.support.Fixtures;
import twila.credicarro.webhooks.model.ProposalEventReason;
import twila.credicarro.webhooks.model.ProposalWebhookEvent;

class CredicarroWebhookEventTest {

    /**
     * Vetor da documentação (webhooks/security): segredo, t, corpo bruto e v1 esperado.
     */
    private static final String SECRET = "segredo-de-exemplo";
    private static final long TIMESTAMP = 1715792400L;
    private static final String BODY = Fixtures.SIGNATURE_VECTOR_BODY;
    private static final String EXPECTED_V1 = "db66c554eb116ef2577deb63f72619793502a31520f622b2d59864b074eb8df2";
    private static final String HEADER = "t=" + TIMESTAMP + ",v1=" + EXPECTED_V1;
    private static final Instant AT_VECTOR_TIME = Instant.ofEpochSecond(TIMESTAMP);

    @Test
    void computesTheDocumentedSignatureVector() {
        assertThat(CredicarroWebhookEvent.computeSignature(SECRET, String.valueOf(TIMESTAMP), BODY)).isEqualTo(EXPECTED_V1);
    }

    @Test
    void acceptsTheDocumentedVector() {
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, CredicarroWebhookEvent.DEFAULT_TOLERANCE, AT_VECTOR_TIME)).isTrue();

        ProposalWebhookEvent event = CredicarroWebhookEvent.parse(BODY, HEADER, SECRET, CredicarroWebhookEvent.DEFAULT_TOLERANCE, AT_VECTOR_TIME);
        assertThat(event.getProposalId()).isEqualTo(UUID.fromString(Fixtures.PROPOSAL_ID));
        assertThat(event.getStatus()).isEqualTo(ProposalStatus.APPROVED);
    }

    @Test
    void acceptsUppercaseHexInTheHeader() {
        String header = "t=" + TIMESTAMP + ",v1=" + EXPECTED_V1.toUpperCase();

        assertThat(CredicarroWebhookEvent.verifySignature(BODY, header, SECRET, CredicarroWebhookEvent.DEFAULT_TOLERANCE, AT_VECTOR_TIME)).isTrue();
    }

    @Test
    void rejectsAWrongSecretOrTamperedBody() {
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, "outro-segredo", Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY.replace("Approved", "Refused"), HEADER, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY + " ", HEADER, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
    }

    @Test
    void rejectsAnEmptySecret() {
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, "", Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, null, Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();

        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, HEADER, "", Duration.ofMinutes(5), AT_VECTOR_TIME))
                .isInstanceOf(CredicarroWebhookSignatureException.class);
    }

    @Test
    void rejectsAnInvalidOrMissingTimestamp() {
        String[] headers = {
                "t=abc,v1=" + EXPECTED_V1,
                "t=,v1=" + EXPECTED_V1,
                "t=12345678901,v1=" + EXPECTED_V1,
                "t=-1715792400,v1=" + EXPECTED_V1,
                "v1=" + EXPECTED_V1,
                "t=" + TIMESTAMP,
                "t=" + TIMESTAMP + ",v1=",
                "não é um header válido",
                "",
                null
        };

        for (String header : headers) {
            assertThat(CredicarroWebhookEvent.verifySignature(BODY, header, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME)).as(String.valueOf(header)).isFalse();
            assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, header, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME))
                    .isInstanceOf(CredicarroWebhookSignatureException.class);
        }
    }

    @Test
    void toleranceAppliesInBothDirections() {
        Duration tolerance = Duration.ofMinutes(5);

        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, tolerance, AT_VECTOR_TIME.plusSeconds(300))).isTrue();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, tolerance, AT_VECTOR_TIME.plusSeconds(301))).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, tolerance, AT_VECTOR_TIME.minusSeconds(300))).isTrue();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, tolerance, AT_VECTOR_TIME.minusSeconds(301))).isFalse();
    }

    @Test
    void toleranceIsConfigurable() {
        Instant tenMinutesLater = AT_VECTOR_TIME.plusSeconds(600);

        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, Duration.ofMinutes(5), tenMinutesLater)).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, Duration.ofMinutes(15), tenMinutesLater)).isTrue();
    }

    @Test
    void usesTheCurrentClockByDefault() {
        long now = Instant.now().getEpochSecond();
        String signature = CredicarroWebhookEvent.computeSignature(SECRET, String.valueOf(now), BODY);
        String header = "t=" + now + ",v1=" + signature;

        assertThat(CredicarroWebhookEvent.verifySignature(BODY, header, SECRET)).isTrue();
        assertThat(CredicarroWebhookEvent.parse(BODY, header, SECRET).getStatus()).isEqualTo(ProposalStatus.APPROVED);

        long old = now - 600;
        String oldHeader = "t=" + old + ",v1=" + CredicarroWebhookEvent.computeSignature(SECRET, String.valueOf(old), BODY);
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, oldHeader, SECRET)).isFalse();
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, oldHeader, SECRET)).isInstanceOf(CredicarroWebhookSignatureException.class);
    }

    @Test
    void errorMessagesNeverContainTheSecret() {
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, "t=" + TIMESTAMP + ",v1=00", SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME))
                .isInstanceOf(CredicarroWebhookSignatureException.class)
                .hasMessageNotContaining(SECRET);
    }

    @Test
    void verifyAuthorizationComparesTheHeaderInConstantTime() {
        assertThat(CredicarroWebhookEvent.verifyAuthorization("Basic dXN1YXJpbzpzZW5oYQ==", "Basic dXN1YXJpbzpzZW5oYQ==")).isTrue();
        assertThat(CredicarroWebhookEvent.verifyAuthorization("Basic dXN1YXJpbzpzZW5oYQ==", "Basic outro")).isFalse();
        assertThat(CredicarroWebhookEvent.verifyAuthorization("Bearer abc", "bearer abc")).isFalse();
        assertThat(CredicarroWebhookEvent.verifyAuthorization(null, "Bearer abc")).isFalse();
        assertThat(CredicarroWebhookEvent.verifyAuthorization("Bearer abc", null)).isFalse();
        assertThat(CredicarroWebhookEvent.verifyAuthorization("", "")).isFalse();
    }

    @Test
    void parsesTheV1PayloadWithEnglishStatusName() {
        ProposalWebhookEvent event = CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"enum_status\":1,\"status\":\"Analysing\"}");

        assertThat(event.getProposalId()).isEqualTo(UUID.fromString(Fixtures.PROPOSAL_ID));
        assertThat(event.getStatus()).isEqualTo(ProposalStatus.ANALYSING);
        assertThat(event.getStatusRaw()).isEqualTo(1);
        assertThat(event.getStatusName()).isEqualTo("Analysing");
        assertThat(event.isDetailed()).isFalse();
        assertThat(event.getBuyer()).isNull();
        assertThat(event.getEventReason()).isNull();
    }

    @Test
    void parsesTheV1PayloadByNameWhenTheCodeIsMissing() {
        ProposalWebhookEvent event = CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"status\":\"ManualAnalysis\"}");

        assertThat(event.getStatus()).isEqualTo(ProposalStatus.MANUAL_ANALYSIS);
        assertThat(event.getStatusRaw()).isEqualTo(5);
    }

    @Test
    void parsesTheV2DetailedPayload() {
        ProposalWebhookEvent event = CredicarroWebhookEvent.parseUnverified(Fixtures.webhookV2());

        assertThat(event.isDetailed()).isTrue();
        assertThat(event.getStatus()).isEqualTo(ProposalStatus.APPROVED);
        assertThat(event.getStatusName()).isEqualTo("APROVADA");
        assertThat(event.getEventReason()).isEqualTo(ProposalEventReason.DOCUMENTS_CHANGED);
        assertThat(event.getEventReasonRaw()).isEqualTo("DOCUMENTOS_ALTERADOS");
        assertThat(event.getRefusalReason()).isNull();
        assertThat(event.getEventDate()).isNotNull();
        assertThat(event.getSeller()).isNull();

        assertThat(event.getBuyer().getDocument()).isEqualTo("12345678909");
        assertThat(event.getBuyer().getActiveStep().getStep()).isEqualTo(ProposalStep.DOCUMENT_CORRECTION);
        assertThat(event.getBuyer().getActiveStep().getActionOwner()).isEqualTo(ActionOwner.INTEGRATOR);
        assertThat(event.getBuyer().getDocuments().get(0).getType()).isEqualTo(DocumentType.CNH);
        assertThat(event.getBuyer().getDocuments().get(0).getStatus()).isEqualTo(DocumentStatus.INVALID);
        assertThat(event.getBuyer().getDocuments().get(0).getReason()).isEqualTo("Documento ilegível");
        assertThat(event.getStore().getDocument()).isEqualTo("11222333000181");
        assertThat(event.getStore().getActiveStep().getStep()).isEqualTo(ProposalStep.DOCUMENT_SUBMISSION);
    }

    @Test
    void unknownStatusKeepsTheRawValue() {
        ProposalWebhookEvent event = CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"enum_status\":9999,\"status\":\"NOVO\"}");

        assertThat(event.getStatus()).isEqualTo(ProposalStatus.UNKNOWN);
        assertThat(event.getStatusRaw()).isEqualTo(9999);
        assertThat(event.getStatusName()).isEqualTo("NOVO");
    }

    @Test
    void invalidBodiesAreRejected() {
        assertThatThrownBy(() -> CredicarroWebhookEvent.parseUnverified("")).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parseUnverified("não é json")).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parseUnverified("null")).isInstanceOf(CredicarroWebhookSignatureException.class);
    }

    @Test
    void bodyWithoutAValidProposalIdIsRejectedEvenWhenTheSignatureIsValid() {
        String body = "{}";
        String header = "t=" + TIMESTAMP + ",v1=" + CredicarroWebhookEvent.computeSignature(SECRET, String.valueOf(TIMESTAMP), body);

        assertThatThrownBy(() -> CredicarroWebhookEvent.parseUnverified("{}")).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"nao-e-uuid\"}")).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(body, header, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME))
                .isInstanceOf(CredicarroWebhookSignatureException.class);
    }

    @Test
    void duplicatedTimestampOrSignatureFieldsInvalidateTheHeader() {
        String duplicatedV1 = HEADER + ",v1=" + EXPECTED_V1;
        String duplicatedT = "t=1,t=" + TIMESTAMP + ",v1=" + EXPECTED_V1;

        assertThat(CredicarroWebhookEvent.verifySignature(BODY, duplicatedV1, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, duplicatedT, SECRET, Duration.ofMinutes(5), AT_VECTOR_TIME)).isFalse();
    }

    @Test
    void toleranceMustBeNonNegative() {
        assertThatThrownBy(() -> CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, HEADER, SECRET, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(CredicarroWebhookEvent.verifySignature(BODY, HEADER, SECRET, Duration.ZERO, AT_VECTOR_TIME)).isTrue();
    }

    @Test
    void verifiedParseRequiresHeaderAndSecret() {
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, null, SECRET)).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, HEADER, null)).isInstanceOf(CredicarroWebhookSignatureException.class);
        assertThatThrownBy(() -> CredicarroWebhookEvent.parse(BODY, HEADER, "")).isInstanceOf(CredicarroWebhookSignatureException.class);
    }

    @Test
    void detailedEventExposesPendingActionsAndV1EventDoesNot() {
        ProposalWebhookEvent detailed = CredicarroWebhookEvent.parseUnverified(Fixtures.webhookV2());
        assertThat(detailed.getPendingActions()).hasSize(2);

        ProposalWebhookEvent v1 = CredicarroWebhookEvent.parseUnverified("{\"id_proposta\":\"" + Fixtures.PROPOSAL_ID + "\",\"enum_status\":2,\"status\":\"Approved\"}");
        assertThat(v1.getPendingActions()).isEmpty();
    }

    @Test
    void toStringMasksPersonalData() {
        String text = CredicarroWebhookEvent.parseUnverified(Fixtures.webhookV2()).toString();

        assertThat(text).doesNotContain("12345678909").doesNotContain("11222333000181").doesNotContain("Documento ilegível");
        assertThat(text).contains("***.456.789-**");
    }
}
