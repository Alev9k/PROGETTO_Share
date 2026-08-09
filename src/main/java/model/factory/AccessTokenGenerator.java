package model.factory;

import java.security.SecureRandom;

/** Generatore di token non predicibili e facili da comunicare. */
public class AccessTokenGenerator {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TOKEN_LENGTH = 12;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH + 2);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                token.append('-');
            }
            token.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return token.toString();
    }
}
