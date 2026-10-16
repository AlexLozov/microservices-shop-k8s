package lab.store.product;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductStore repository;

    public ProductService(ProductStore repository) { this.repository = repository; }

    public List<Map<String, Object>> list() { return repository.list(); }

    public Map<String, Object> get(String id) {
        return repository.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Map<String, Object> create(ProductInput input, String role) {
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
        String id = UUID.randomUUID().toString();
        repository.save(id, input.name().trim(), input.price(), input.stock());
        return get(id);
    }

    public Map<String, Object> reserve(String id, int quantity) {
        validateQuantity(quantity);
        if (repository.reserve(id, quantity) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient stock or product not found");
        }
        return get(id);
    }

    public Map<String, Object> release(String id, int quantity) {
        validateQuantity(quantity);
        repository.release(id, quantity);
        return get(id);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
        }
    }
}
