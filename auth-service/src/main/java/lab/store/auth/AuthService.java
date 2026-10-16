package lab.store.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.store.common.Tokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final CredentialStore repository;
    private final ProfileGateway profiles;
    private final ObjectMapper mapper;
    private final String jwtSecret;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(CredentialStore repository, ProfileGateway profiles, ObjectMapper mapper,
                       @Value("${app.jwt-secret}") String jwtSecret) {
        this.repository = repository;
        this.profiles = profiles;
        this.mapper = mapper;
        this.jwtSecret = jwtSecret;
    }

    public Map<String, String> register(RegisterRequest request) {
        String id = UUID.randomUUID().toString();
        String email = request.email().trim().toLowerCase();
        repository.save(id, email, encoder.encode(request.password()), "USER");
        try {
            profiles.create(id, request.name().trim(), email);
        } catch (RuntimeException ex) {
            repository.delete(id);
            throw ex;
        }
        return Map.of("id", id, "accessToken", Tokens.issue(id, "USER", jwtSecret, mapper));
    }

    public Map<String, String> login(LoginRequest request) {
        Credential credential = repository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(this::invalidCredentials);
        if (!encoder.matches(request.password(), credential.passwordHash())) {
            throw invalidCredentials();
        }
        return Map.of("accessToken", Tokens.issue(credential.id(), credential.role(), jwtSecret, mapper));
    }

    public String hashPassword(String password) { return encoder.encode(password); }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}
