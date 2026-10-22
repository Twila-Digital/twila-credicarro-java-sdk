package twila.credicarro.webhooks;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import twila.credicarro.errors.CredicarroWebhookSignatureException;
import twila.credicarro.internal.generated.webhook.ProposalWebhookEventWire;
import twila.credicarro.internal.mapping.WebhookMapper;
import twila.credicarro.internal.util.HexUtility;
import twila.credicarro.serialization.CredicarroObjectMapper;
import twila.credicarro.webhooks.model.ProposalWebhookEvent;

/**
 * Leitura e verificação das notificações do webhook de proposta.
 *
 * <p>Assinatura: cabeçalho {@code X-CrediCarro-Signature: t={timestamp},v1={assinatura}}, onde {@code v1} é o
 * HMAC-SHA256 em hexadecimal minúsculo de {@code "{t}.{corpo bruto}"}, com o {@code segredo_assinatura} (texto em
 * UTF-8) como chave. Valide sempre contra o <b>corpo bruto</b>, antes de desserializar. {@code t} ou {@code v1}
 * duplicados no cabeçalho tornam a assinatura inválida.
 */
public final class CredicarroWebhookEvent {

    /**
     * Tolerância padrão (nos dois sentidos) entre o {@code t} do cabeçalho e o relógio local.
     */
    public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Pattern TIMESTAMP = Pattern.compile("^\\d{1,10}$");
    private static final String INVALID_BODY = "O corpo do webhook está vazio, não é um JSON válido ou não traz um id_proposta válido.";

    private CredicarroWebhookEvent() {
    }

    /**
     * Lê o evento <b>sem</b> verificar a assinatura — só para testes. Em produção use {@link #parse(String, String, String)}.
     * Aceita os payloads v1 e v2; exige {@code id_proposta} UUID válido.
     */
    public static ProposalWebhookEvent parseUnverified(String rawBody) {
        ProposalWebhookEventWire wire;
        try {
            wire = CredicarroObjectMapper.DEFAULT.readValue(rawBody, ProposalWebhookEventWire.class);
        } catch (Exception ex) {
            throw new CredicarroWebhookSignatureException(INVALID_BODY);
        }

        if (wire == null || wire.idProposta == null)
            throw new CredicarroWebhookSignatureException(INVALID_BODY);

        return WebhookMapper.toPublic(wire);
    }

    /**
     * Verifica a assinatura (tolerância de 5 minutos) e lê o evento. Cabeçalho e segredo são obrigatórios.
     *
     * @throws CredicarroWebhookSignatureException se a assinatura for inválida ou o corpo não tiver {@code id_proposta}
     */
    public static ProposalWebhookEvent parse(String rawBody, String signatureHeader, String signingSecret) {
        return parse(rawBody, signatureHeader, signingSecret, DEFAULT_TOLERANCE);
    }

    public static ProposalWebhookEvent parse(String rawBody, String signatureHeader, String signingSecret, Duration tolerance) {
        return parse(rawBody, signatureHeader, signingSecret, tolerance, Instant.now());
    }

    static ProposalWebhookEvent parse(String rawBody, String signatureHeader, String signingSecret, Duration tolerance, Instant now) {
        requireValidTolerance(tolerance);

        String failure = checkSignature(rawBody, signatureHeader, signingSecret, tolerance, now);
        if (failure != null)
            throw new CredicarroWebhookSignatureException(failure);

        return parseUnverified(rawBody);
    }

    /**
     * Verifica a assinatura sem lançar exceção: {@code false} para segredo vazio, cabeçalho malformado ou duplicado,
     * assinatura que não confere ou {@code t} fora da tolerância (5 minutos).
     */
    public static boolean verifySignature(String rawBody, String signatureHeader, String signingSecret) {
        return verifySignature(rawBody, signatureHeader, signingSecret, DEFAULT_TOLERANCE);
    }

    public static boolean verifySignature(String rawBody, String signatureHeader, String signingSecret, Duration tolerance) {
        return verifySignature(rawBody, signatureHeader, signingSecret, tolerance, Instant.now());
    }

    static boolean verifySignature(String rawBody, String signatureHeader, String signingSecret, Duration tolerance, Instant now) {
        requireValidTolerance(tolerance);
        return checkSignature(rawBody, signatureHeader, signingSecret, tolerance, now) == null;
    }

    /**
     * Compara em tempo constante o cabeçalho {@code Authorization} recebido com o esperado (o configurado no
     * cadastro do webhook, ex.: {@code Basic dXN1YXJpbzpzZW5oYQ==} ou {@code Bearer <token>}). Compara o SHA-256 dos
     * dois valores, então o tamanho não vaza. Valores nulos ou o esperado vazio nunca aceitam.
     */
    public static boolean verifyAuthorization(String receivedHeader, String expectedHeader) {
        if (receivedHeader == null || expectedHeader == null || expectedHeader.isEmpty())
            return false;

        return MessageDigest.isEqual(sha256(receivedHeader), sha256(expectedHeader));
    }

    /**
     * Calcula o {@code v1}: HMAC-SHA256 hex minúsculo de {@code "{timestamp}.{corpo}"}.
     */
    static String computeSignature(String signingSecret, String timestamp, String rawBody) {
        String signedContent = timestamp + "." + rawBody;

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexUtility.toHexStringLower(mac.doFinal(signedContent.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Falha ao calcular a assinatura HMAC-SHA256.", ex);
        }
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível.", ex);
        }
    }

    private static void requireValidTolerance(Duration tolerance) {
        if (tolerance == null || tolerance.isNegative())
            throw new IllegalArgumentException("A tolerância deve ser uma duração maior ou igual a zero.");
    }

    /**
     * Devolve {@code null} quando a assinatura é válida, ou o motivo da falha. A mensagem nunca contém o segredo.
     */
    private static String checkSignature(String rawBody, String signatureHeader, String signingSecret, Duration tolerance, Instant now) {
        if (signingSecret == null || signingSecret.isEmpty())
            return "O segredo de assinatura do webhook está vazio.";

        if (rawBody == null)
            return "O corpo do webhook é obrigatório para verificar a assinatura.";

        SignatureHeader header = SignatureHeader.parse(signatureHeader);
        if (header == null)
            return "Cabeçalho de assinatura malformado: esperado t={timestamp},v1={assinatura}, sem campos repetidos.";

        byte[] expected = computeSignature(signingSecret, header.timestamp, rawBody).getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expected, header.signature.getBytes(StandardCharsets.UTF_8)))
            return "A assinatura do webhook não confere.";

        Duration skew = Duration.between(Instant.ofEpochSecond(Long.parseLong(header.timestamp)), now).abs();
        if (skew.compareTo(tolerance) > 0)
            return "O timestamp do webhook está fora da janela de tolerância — possível replay.";

        return null;
    }

    /**
     * Cabeçalho {@code X-CrediCarro-Signature} ({@code t={timestamp},v1={assinatura}}).
     */
    private static final class SignatureHeader {
        private final String timestamp;
        private final String signature;

        private SignatureHeader(String timestamp, String signature) {
            this.timestamp = timestamp;
            this.signature = signature;
        }

        /**
         * Devolve {@code null} se o cabeçalho faltar, tiver campo vazio ou repetido, ou timestamp não numérico.
         */
        static SignatureHeader parse(String value) {
            if (value == null)
                return null;

            String timestamp = null;
            String signature = null;

            for (String part : value.split(",")) {
                String[] pair = part.trim().split("=", 2);
                if (pair.length != 2)
                    continue;

                if ("t".equals(pair[0])) {
                    if (timestamp != null)
                        return null;
                    timestamp = TIMESTAMP.matcher(pair[1]).matches() ? pair[1] : "";
                } else if ("v1".equals(pair[0])) {
                    if (signature != null)
                        return null;
                    signature = pair[1].toLowerCase(Locale.ROOT);
                }
            }

            boolean complete = timestamp != null && !timestamp.isEmpty() && signature != null && !signature.isEmpty();
            return complete ? new SignatureHeader(timestamp, signature) : null;
        }
    }
}
