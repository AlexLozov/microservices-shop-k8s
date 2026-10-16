package lab.store.payment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentStore repository;
    private final OrderClient orders;
    private final PaymentEventPublisher events;

    public PaymentService(PaymentStore repository, OrderClient orders, PaymentEventPublisher events) {
        this.repository = repository;
        this.orders = orders;
        this.events = events;
    }

    public Map<String, Object> pay(NewPaymentRequest request, String userId) {
        OrderInfo order = orders.get(request.orderId());
        if (!userId.equals(order.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order belongs to another user");
        }
        if (!"PENDING".equals(order.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not pending");
        }
        String id = UUID.randomUUID().toString();
        repository.save(id, request.orderId(), userId, order.total());
        orders.markPaid(request.orderId());
        events.completed(id, request.orderId(), userId);
        return get(id, userId);
    }

    public Map<String, Object> get(String id, String userId) {
        return repository.findForUser(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public List<Map<String, Object>> list(String userId) { return repository.listForUser(userId); }
}
