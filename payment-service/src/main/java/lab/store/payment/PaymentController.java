package lab.store.payment;

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
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping("/api/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> pay(@Valid @RequestBody NewPaymentRequest request, HttpServletRequest http) {
        return service.pay(request, (String) http.getAttribute("userId"));
    }

    @GetMapping("/api/payments/{id}")
    public Map<String, Object> get(@PathVariable String id, HttpServletRequest http) {
        return service.get(id, (String) http.getAttribute("userId"));
    }

    @GetMapping("/api/payments")
    public List<Map<String, Object>> list(HttpServletRequest http) {
        return service.list((String) http.getAttribute("userId"));
    }
}
