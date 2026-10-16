package lab.store.payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentStore {
    void save(String id, String orderId, String userId, BigDecimal amount);
    Optional<Map<String, Object>> findForUser(String id, String userId);
    List<Map<String, Object>> listForUser(String userId);
}
