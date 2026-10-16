package lab.store.notification;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationStore repository;

    public NotificationService(NotificationStore repository) { this.repository = repository; }

    public void onPaymentCompleted(String rawEvent) {
        PaymentCompletedEvent event = PaymentCompletedEvent.parse(rawEvent);
        try {
            repository.save(UUID.randomUUID().toString(), event);
        } catch (DuplicateKeyException ignored) {
            // RabbitMQ may redeliver an event; payment_id is unique.
        }
    }

    public List<Map<String, Object>> list(String userId) { return repository.listForUser(userId); }
}
