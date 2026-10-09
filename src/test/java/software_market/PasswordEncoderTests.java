package software_market;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTests {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void samePasswordProducesDifferentSaltedHashes() {
        String raw = "admin123";
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertNotEquals(hash1, hash2, "两次加密应使用不同盐值");
        assertTrue(encoder.matches(raw, hash1));
        assertTrue(encoder.matches(raw, hash2));
        assertFalse(encoder.matches("wrong-password", hash1));
    }
}
