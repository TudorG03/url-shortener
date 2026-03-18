package eu.deic.url_shortener.util;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private CodeGenerator() {
    }

    public static String generate(int length) {
        char[] code = new char[length];
        for (int i = 0; i < length; i++) {
            code[i] = ALPHABET.charAt(RNG.nextInt(0, ALPHABET.length()));
        }

        return new String(code);
    }
}
