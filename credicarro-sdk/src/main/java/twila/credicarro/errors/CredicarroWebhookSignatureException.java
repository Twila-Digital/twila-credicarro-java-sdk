package twila.credicarro.errors;

/**
 * Lançada quando a assinatura de um payload de webhook não é válida (não confere com o segredo esperado,
 * cabeçalho malformado, segredo vazio ou timestamp fora da janela de tolerância de replay), ou quando o corpo
 * não é um JSON válido.
 */
public final class CredicarroWebhookSignatureException extends CredicarroException {

    public CredicarroWebhookSignatureException(String message) {
        super(message);
    }
}
