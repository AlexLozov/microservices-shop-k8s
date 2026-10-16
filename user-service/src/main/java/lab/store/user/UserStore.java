package lab.store.user;

import java.util.Map;
import java.util.Optional;

public interface UserStore {
    void save(String id, String name, String email);
    Optional<Map<String, Object>> find(String id);
}
