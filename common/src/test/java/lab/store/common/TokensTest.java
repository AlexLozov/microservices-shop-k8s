package lab.store.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokensTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String secret = "test-secret-with-at-least-32-characters";

    @Test void issueAndVerify() throws Exception {
        var token = Tokens.issue("user-1", "USER", secret, mapper);
        var claims = Tokens.verify(token, secret, mapper);
        assertEquals("user-1", claims.get("sub"));
        assertEquals("USER", claims.get("role"));
    }

    @Test void rejectsTamperingAndWrongSecret() {
        var token = Tokens.issue("user-1", "USER", secret, mapper);
        assertThrows(Exception.class, () -> Tokens.verify(token, "another-secret-with-at-least-32-chars", mapper));
        var pieces = token.split("\\.");
        var tampered = pieces[0] + "." + pieces[1] + ".invalid";
        assertThrows(Exception.class, () -> Tokens.verify(tampered, secret, mapper));
    }
}
