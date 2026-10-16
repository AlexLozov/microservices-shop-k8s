package lab.store.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private final PaymentStore repository = mock(PaymentStore.class);
    private final OrderClient orders = mock(OrderClient.class);
    private final PaymentEventPublisher events = mock(PaymentEventPublisher.class);
    private final PaymentService service = new PaymentService(repository, orders, events);

    @Test
    void completesOwnedPendingOrderAndPublishesEvent() {
        when(orders.get("o1")).thenReturn(new OrderInfo("u1", new BigDecimal("25.00"), "PENDING"));
        when(repository.findForUser(anyString(), eq("u1"))).thenAnswer(call -> Optional.of(
                Map.of("ID", call.getArgument(0), "STATUS", "COMPLETED")));
        var payment = service.pay(new NewPaymentRequest("o1"), "u1");
        String id = (String) payment.get("ID");
        verify(repository).save(id, "o1", "u1", new BigDecimal("25.00"));
        verify(orders).markPaid("o1");
        verify(events).completed(id, "o1", "u1");
    }

    @Test
    void deniesPaymentForAnotherUsersOrder() {
        when(orders.get("o1")).thenReturn(new OrderInfo("u2", BigDecimal.ONE, "PENDING"));
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.pay(new NewPaymentRequest("o1"), "u1"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(repository, events);
    }

    @Test
    void cannotPayAnAlreadyPaidOrder() {
        when(orders.get("o1")).thenReturn(new OrderInfo("u1", BigDecimal.ONE, "PAID"));
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.pay(new NewPaymentRequest("o1"), "u1"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verifyNoInteractions(repository, events);
    }
}
