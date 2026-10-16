package lab.store.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminSeeder implements CommandLineRunner {
    private final CredentialStore repository;
    private final AuthService auth;
    private final String email;
    private final String password;

    public AdminSeeder(CredentialStore repository, AuthService auth,
                       @Value("${app.admin-email:admin@example.com}") String email,
                       @Value("${app.admin-password}") String password) {
        this.repository = repository;
        this.auth = auth;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!repository.hasAdmin()) {
            repository.save(UUID.randomUUID().toString(), email.trim().toLowerCase(),
                    auth.hashPassword(password), "ADMIN");
        }
    }
}
