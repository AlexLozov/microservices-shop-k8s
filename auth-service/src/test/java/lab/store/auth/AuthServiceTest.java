package lab.store.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.store.common.Tokens;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final CredentialStore repository = mock(CredentialStore.class);
    private final ProfileGateway profiles = mock(ProfileGateway.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final String secret = "test-secret-with-at-least-32-characters";
    private final AuthService service = new AuthService(repository, profiles, mapper, secret);

    @Test
    void registrationCreatesCredentialAndProfile() throws Exception {
        var result = service.register(new RegisterRequest("  Alex  ", "  ALEX@example.com  ", "Pass12345!"));
        var hash = ArgumentCaptor.forClass(String.class);
        verify(repository).save(eq(result.get("id")), eq("alex@example.com"), hash.capture(), eq("USER"));
        assertTrue(new BCryptPasswordEncoder().matches("Pass12345!", hash.getValue()));
        verify(profiles).create(result.get("id"), "Alex", "alex@example.com");
        assertEquals(result.get("id"), Tokens.verify(result.get("accessToken"), secret, mapper).get("sub"));
    }

    @Test
    void registrationRemovesCredentialWhenProfileCreationFails() {
        doThrow(new IllegalStateException("user service down")).when(profiles)
                .create(anyString(), anyString(), anyString());
        assertThrows(IllegalStateException.class,
                () -> service.register(new RegisterRequest("Alex", "a@example.com", "Pass12345!")));
        var id = ArgumentCaptor.forClass(String.class);
        verify(repository).save(id.capture(), anyString(), anyString(), eq("USER"));
        verify(repository).delete(id.getValue());
    }

    @Test
    void loginRejectsWrongPassword() {
        when(repository.findByEmail("a@example.com")).thenReturn(Optional.of(
                new Credential("u1", new BCryptPasswordEncoder().encode("correct123"), "USER")));
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("a@example.com", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
