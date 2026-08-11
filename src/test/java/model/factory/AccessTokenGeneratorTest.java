package model.factory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTokenGeneratorTest {

    @Test
    void generatedTokenContainsExactlySixDigits() {
        AccessTokenGenerator generator = new AccessTokenGenerator();

        for (int i = 0; i < 1_000; i++) {
            assertTrue(generator.generate().matches("\\d{6}"));
        }
    }
}
