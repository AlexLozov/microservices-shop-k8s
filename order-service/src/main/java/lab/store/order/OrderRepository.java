package lab.store.order;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderRepository implements OrderStore {
    private final JdbcTemplate db;

    public OrderRepository(JdbcTemplate db) { this.db = db; }

    public void save(String id, String userId, String productId, int quantity, BigDecimal total) {
        db.update("INSERT INTO orders(id,user_id,product_id,quantity,total,status) VALUES(?,?,?,?,?,?)",
                id, userId, productId, quantity, total, "PENDING");
    }

    public List<Map<String, Object>> listForUser(String userId) {
        return db.queryForList("SELECT * FROM orders WHERE user_id=? ORDER BY created_at DESC", userId);
    }

    public Optional<Map<String, Object>> find(String id) {
        return db.queryForList("SELECT * FROM orders WHERE id=?", id).stream().findFirst();
    }

    public int markPaid(String id) {
        return db.update("UPDATE orders SET status='PAID' WHERE id=? AND status='PENDING'", id);
    }
}
