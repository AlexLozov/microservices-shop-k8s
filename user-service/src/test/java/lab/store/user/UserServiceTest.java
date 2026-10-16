package lab.store.user;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserStore repository = mock(UserStore.class);
    private final UserService service = new UserService(repository);

    @Test
    void createsNormalizedProfile() {
        assertEquals("u1", service.create(new NewUserRequest("u1", "  Alex  ", " A@Example.com ")).get("id"));
        verify(repository).save("u1", "Alex", "a@example.com");
    }

    @Test
    void deniesAnotherUsersProfile() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.profile("u2", "u1", "USER"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void ownerCanReadProfileAndMissingUserIsNotFound() {
        when(repository.find("u1")).thenReturn(Optional.of(Map.of("ID", "u1")));
        assertEquals("u1", service.profile("u1", "u1", "USER").get("ID"));
        var ex = assertThrows(ResponseStatusException.class, () -> service.ownProfile("missing"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
