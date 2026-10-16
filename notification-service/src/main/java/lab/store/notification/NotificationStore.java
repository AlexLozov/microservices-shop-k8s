package lab.store.notification;

import java.util.List;
import java.util.Map;

public interface NotificationStore {
    void save(String id, PaymentCompletedEvent event);
    List<Map<String, Object>> listForUser(String userId);
}
