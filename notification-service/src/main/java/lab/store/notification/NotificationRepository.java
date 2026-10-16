package lab.store.notification;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NotificationRepository implements NotificationStore {
    private final JdbcTemplate db;

    public NotificationRepository(JdbcTemplate db) { this.db = db; }

    public void save(String id, PaymentCompletedEvent event) {
        db.update("INSERT INTO notifications(id,payment_id,order_id,user_id,message) VALUES(?,?,?,?,?)",
                id, event.paymentId(), event.orderId(), event.userId(),
                "Payment received for order " + event.orderId());
    }

    public List<Map<String, Object>> listForUser(String userId) {
        return db.queryForList("SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC", userId);
    }
}
