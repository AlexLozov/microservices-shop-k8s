package lab.store.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class UserService {
    private final UserStore repository;

    public UserService(UserStore repository) { this.repository = repository; }

    public Map<String, String> create(NewUserRequest request) {
        repository.save(request.id(), request.name().trim(), request.email().trim().toLowerCase());
        return Map.of("id", request.id());
    }

    public Map<String, Object> ownProfile(String userId) { return find(userId); }

    public Map<String, Object> profile(String id, String requesterId, String role) {
        if (!id.equals(requesterId) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User profile belongs to another user");
        }
        return find(id);
    }

    private Map<String, Object> find(String id) {
        return repository.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
