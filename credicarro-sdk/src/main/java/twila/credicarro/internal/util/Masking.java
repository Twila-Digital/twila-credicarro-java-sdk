package twila.credicarro.internal.util;

/**
 * Máscaras para {@code toString} de requisições e respostas: dados pessoais, bancários, credenciais e segredos nunca
 * aparecem em log por acidente.
 */
public final class Masking {

    public static final String MASK = "***";

    private Masking() {
    }

    /**
     * CPF de 11 dígitos vira {@code ***.456.789-**}; qualquer outro valor (inclusive CNPJ) vira {@code ***}.
     */
    public static String cpf(String value) {
        if (value == null)
            return null;

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9')
                digits.append(c);
        }

        if (digits.length() != 11)
            return MASK;

        return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
    }

    /**
     * Mascara qualquer valor presente; {@code null} continua {@code null}.
     */
    public static String mask(Object value) {
        return value == null ? null : MASK;
    }

    /**
     * Monta {@code Nome[campo=valor, ...]} a partir de pares nome/valor (valores já mascarados pelo chamador).
     */
    public static String describe(String name, Object... pairs) {
        StringBuilder text = new StringBuilder(name).append('[');

        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (i > 0)
                text.append(", ");

            text.append(pairs[i]).append('=').append(pairs[i + 1]);
        }

        return text.append(']').toString();
    }
}
