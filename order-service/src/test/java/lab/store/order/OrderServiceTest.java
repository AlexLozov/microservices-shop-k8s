package lab.store.order;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private final OrderStore repository = mock(OrderStore.class);
    private final ProductClient products = mock(ProductClient.class);
    private final OrderService service = new OrderService(repository, products);

    @Test
    void calculatesTotalAndReservesProduct() {
        when(products.price("p1")).thenReturn(new BigDecimal("12.50"));
        when(repository.find(anyString())).thenAnswer(call -> Optional.of(
                Map.of("ID", call.getArgument(0), "USER_ID", "u1", "STATUS", "PENDING")));
        var order = service.create(new NewOrderRequest("p1", 3), "u1");
        verify(products).reserve("p1", 3);
        verify(repository).save((String) order.get("ID"), "u1", "p1", 3, new BigDecimal("37.50"));
    }

    @Test
    void releasesReservationWhenSavingFails() {
        when(products.price("p1")).thenReturn(new BigDecimal("10.00"));
        doThrow(new IllegalStateException("database down")).when(repository)
                .save(anyString(), eq("u1"), eq("p1"), eq(2), any(BigDecimal.class));
        assertThrows(IllegalStateException.class,
                () -> service.create(new NewOrderRequest("p1", 2), "u1"));
        verify(products).release("p1", 2);
    }

    @Test
    void preventsReadingAnotherUsersOrder() {
        when(repository.find("o1")).thenReturn(Optional.of(Map.of("USER_ID", "u2")));
        var ex = assertThrows(ResponseStatusException.class, () -> service.get("o1", "u1", "USER"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void paidTransitionIsIdempotent() {
        when(repository.markPaid("o1")).thenReturn(0);
        when(repository.find("o1")).thenReturn(Optional.of(Map.of("STATUS", "PAID")));
        assertEquals("PAID", service.paid("o1").get("STATUS"));
    }
}
