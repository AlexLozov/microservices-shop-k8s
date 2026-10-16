package lab.store.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class UserProfileClient implements ProfileGateway {
    private final RestClient rest;
    private final String userUrl;
    private final String internalKey;

    public UserProfileClient(RestClient rest, @Value("${services.user-url}") String userUrl,
                             @Value("${app.internal-key}") String internalKey) {
        this.rest = rest;
        this.userUrl = userUrl;
        this.internalKey = internalKey;
    }

    public void create(String id, String name, String email) {
        rest.post().uri(userUrl + "/internal/users").header("X-Internal-Key", internalKey)
                .body(Map.of("id", id, "name", name, "email", email))
                .retrieve().toBodilessEntity();
    }
}
