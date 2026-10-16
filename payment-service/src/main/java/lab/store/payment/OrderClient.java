package lab.store.payment;

public interface OrderClient {
    OrderInfo get(String orderId);
    void markPaid(String orderId);
}
