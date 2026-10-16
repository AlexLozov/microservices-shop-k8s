package lab.store.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthRepository implements CredentialStore {
    private final JdbcTemplate db;

    public AuthRepository(JdbcTemplate db) { this.db = db; }

    public void save(String id, String email, String hash, String role) {
        db.update("INSERT INTO credentials(id,email,password_hash,role) VALUES(?,?,?,?)",
                id, email, hash, role);
    }

    public Optional<Credential> findByEmail(String email) {
        return db.query("SELECT id,password_hash,role FROM credentials WHERE email=?",
                (rs, rowNum) -> new Credential(rs.getString("id"),
                        rs.getString("password_hash"), rs.getString("role")), email)
                .stream().findFirst();
    }

    public boolean hasAdmin() {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM credentials WHERE role='ADMIN'", Integer.class);
        return count != null && count > 0;
    }

    public void delete(String id) { db.update("DELETE FROM credentials WHERE id=?", id); }
}
