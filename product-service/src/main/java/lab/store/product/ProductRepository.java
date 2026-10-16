package lab.store.product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository implements ProductStore {
    private final JdbcTemplate db;

    public ProductRepository(JdbcTemplate db) { this.db = db; }

    public List<Map<String, Object>> list() {
        return db.queryForList("SELECT id,name,price,stock FROM products ORDER BY name");
    }

    public Optional<Map<String, Object>> find(String id) {
        return db.queryForList("SELECT id,name,price,stock FROM products WHERE id=?", id)
                .stream().findFirst();
    }

    public void save(String id, String name, BigDecimal price, int stock) {
        db.update("INSERT INTO products(id,name,price,stock) VALUES(?,?,?,?)", id, name, price, stock);
    }

    public int reserve(String id, int quantity) {
        return db.update("UPDATE products SET stock=stock-? WHERE id=? AND stock>=?",
                quantity, id, quantity);
    }

    public void release(String id, int quantity) {
        db.update("UPDATE products SET stock=stock+? WHERE id=?", quantity, id);
    }
}
