package lab.store.payment;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitPaymentEventPublisher implements PaymentEventPublisher {
    private final RabbitTemplate rabbit;

    public RabbitPaymentEventPublisher(RabbitTemplate rabbit) { this.rabbit = rabbit; }

    @Override
    public void completed(String paymentId, String orderId, String userId) {
        rabbit.convertAndSend("payment.completed", paymentId + ":" + orderId + ":" + userId);
    }
}
