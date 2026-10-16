package lab.store.payment;

public interface PaymentEventPublisher {
    void completed(String paymentId, String orderId, String userId);
}
