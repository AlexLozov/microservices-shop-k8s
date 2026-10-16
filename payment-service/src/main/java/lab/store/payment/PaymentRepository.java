package lab.store.payment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentRepository implements PaymentStore {
    private final JdbcTemplate db;

    public PaymentRepository(JdbcTemplate db) { this.db = db; }

    public void save(String id, String orderId, String userId, BigDecimal amount) {
        db.update("INSERT INTO payments(id,order_id,user_id,amount,status) VALUES(?,?,?,?,?)",
                id, orderId, userId, amount, "COMPLETED");
    }

    public Optional<Map<String, Object>> findForUser(String id, String userId) {
        return db.queryForList("SELECT * FROM payments WHERE id=? AND user_id=?", id, userId)
                .stream().findFirst();
    }

    public List<Map<String, Object>> listForUser(String userId) {
        return db.queryForList("SELECT * FROM payments WHERE user_id=? ORDER BY created_at DESC", userId);
    }
}
