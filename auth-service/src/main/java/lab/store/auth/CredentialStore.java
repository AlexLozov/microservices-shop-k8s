package lab.store.auth;

import java.util.Optional;

public interface CredentialStore {
    void save(String id, String email, String hash, String role);
    Optional<Credential> findByEmail(String email);
    boolean hasAdmin();
    void delete(String id);
}
