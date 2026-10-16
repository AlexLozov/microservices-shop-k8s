package lab.store.order;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderStore repository;
    private final ProductClient products;

    public OrderService(OrderStore repository, ProductClient products) {
        this.repository = repository;
        this.products = products;
    }

    public Map<String, Object> create(NewOrderRequest request, String userId) {
        BigDecimal total = products.price(request.productId())
                .multiply(BigDecimal.valueOf(request.quantity()));
        products.reserve(request.productId(), request.quantity());
        String id = UUID.randomUUID().toString();
        try {
            repository.save(id, userId, request.productId(), request.quantity(), total);
        } catch (RuntimeException ex) {
            products.release(request.productId(), request.quantity());
            throw ex;
        }
        return internalGet(id);
    }

    public List<Map<String, Object>> list(String userId) { return repository.listForUser(userId); }

    public Map<String, Object> get(String id, String userId, String role) {
        Map<String, Object> order = internalGet(id);
        if (!order.get("USER_ID").equals(userId) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order belongs to another user");
        }
        return order;
    }

    public Map<String, Object> internalGet(String id) {
        return repository.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public Map<String, Object> paid(String id) {
        int changed = repository.markPaid(id);
        if (changed == 0 && !"PAID".equals(internalGet(id).get("STATUS"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order cannot be paid");
        }
        return internalGet(id);
    }
}
