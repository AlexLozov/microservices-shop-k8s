package lab.store.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository implements UserStore {
    private final JdbcTemplate db;

    public UserRepository(JdbcTemplate db) { this.db = db; }

    public void save(String id, String name, String email) {
        db.update("INSERT INTO users(id,name,email) VALUES(?,?,?)", id, name, email);
    }

    public Optional<Map<String, Object>> find(String id) {
        return db.queryForList("SELECT id,name,email FROM users WHERE id=?", id).stream().findFirst();
    }
}
