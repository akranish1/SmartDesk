package smartdesk.booking.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldHashAndVerifyPassword() {

        String rawPassword = "password123";

        String hash = passwordEncoder.encode(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);

        assertTrue(
                passwordEncoder.matches(rawPassword, hash)
        );

        assertFalse(
                passwordEncoder.matches("wrongPassword", hash)
        );
    }
}