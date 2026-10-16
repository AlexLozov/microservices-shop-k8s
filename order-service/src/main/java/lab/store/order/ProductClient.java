package lab.store.order;

import java.math.BigDecimal;

public interface ProductClient {
    BigDecimal price(String productId);
    void reserve(String productId, int quantity);
    void release(String productId, int quantity);
}
