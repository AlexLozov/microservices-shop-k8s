package lab.store.order;

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
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody NewOrderRequest request, HttpServletRequest http) {
        return service.create(request, (String) http.getAttribute("userId"));
    }

    @GetMapping("/api/orders")
    public List<Map<String, Object>> list(HttpServletRequest http) {
        return service.list((String) http.getAttribute("userId"));
    }

    @GetMapping("/api/orders/{id}")
    public Map<String, Object> get(@PathVariable String id, HttpServletRequest http) {
        return service.get(id, (String) http.getAttribute("userId"), (String) http.getAttribute("role"));
    }

    @GetMapping("/internal/orders/{id}")
    public Map<String, Object> internalGet(@PathVariable String id) { return service.internalGet(id); }

    @PostMapping("/internal/orders/{id}/paid")
    public Map<String, Object> paid(@PathVariable String id) { return service.paid(id); }
}
