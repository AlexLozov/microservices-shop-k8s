package lab.store.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderStore {
    void save(String id, String userId, String productId, int quantity, BigDecimal total);
    List<Map<String, Object>> listForUser(String userId);
    Optional<Map<String, Object>> find(String id);
    int markPaid(String id);
}
