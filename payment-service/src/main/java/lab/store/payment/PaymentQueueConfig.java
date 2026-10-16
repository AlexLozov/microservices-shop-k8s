package lab.store.payment;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentQueueConfig {
    @Bean
    Queue paymentQueue() { return new Queue("payment.completed", true); }
}
