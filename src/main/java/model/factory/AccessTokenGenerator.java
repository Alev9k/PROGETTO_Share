package model.factory;

import java.security.SecureRandom;
import java.util.Locale;

/** Generatore di token non predicibili e facili da comunicare. */
public class AccessTokenGenerator {
    private static final int TOKEN_BOUND = 1_000_000;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return String.format(Locale.ROOT, "%06d", random.nextInt(TOKEN_BOUND));
    }
}
