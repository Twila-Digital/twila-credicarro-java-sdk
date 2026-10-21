package twila.credicarro.internal.util;

public final class HexUtility {

    private static final char[] HEX_ALPHABET = "0123456789abcdef".toCharArray();

    private HexUtility() {
    }

    public static String toHexStringLower(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = HEX_ALPHABET[(bytes[i] >> 4) & 0xF];
            chars[i * 2 + 1] = HEX_ALPHABET[bytes[i] & 0xF];
        }

        return new String(chars);
    }
}
