package lab.store.notification;

public record PaymentCompletedEvent(String paymentId, String orderId, String userId) {
    public static PaymentCompletedEvent parse(String raw) {
        String[] parts = raw.split(":", 3);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("Invalid payment event");
        }
        return new PaymentCompletedEvent(parts[0], parts[1], parts[2]);
    }
}
