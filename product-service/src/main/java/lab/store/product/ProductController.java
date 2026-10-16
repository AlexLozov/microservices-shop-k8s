package lab.store.product;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @GetMapping("/api/products")
    public List<Map<String, Object>> list() { return service.list(); }

    @GetMapping("/api/products/{id}")
    public Map<String, Object> get(@PathVariable String id) { return service.get(id); }

    @PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody ProductInput input, HttpServletRequest request) {
        return service.create(input, (String) request.getAttribute("role"));
    }

    @PostMapping("/internal/products/{id}/reserve")
    public Map<String, Object> reserve(@PathVariable String id,
                                       @Valid @RequestBody QuantityRequest request) {
        return service.reserve(id, request.quantity());
    }

    @PostMapping("/internal/products/{id}/release")
    public Map<String, Object> release(@PathVariable String id,
                                       @Valid @RequestBody QuantityRequest request) {
        return service.release(id, request.quantity());
    }
}
