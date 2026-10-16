package lab.store.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private final NotificationService service;

    public PaymentEventListener(NotificationService service) { this.service = service; }

    @RabbitListener(queues = "payment.completed")
    public void receive(String event) { service.onPaymentCompleted(event); }
}
