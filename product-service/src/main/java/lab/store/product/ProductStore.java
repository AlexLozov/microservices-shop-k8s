package lab.store.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductStore {
    List<Map<String, Object>> list();
    Optional<Map<String, Object>> find(String id);
    void save(String id, String name, BigDecimal price, int stock);
    int reserve(String id, int quantity);
    void release(String id, int quantity);
}
