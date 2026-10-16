package lab.store.notification;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private final NotificationStore repository = mock(NotificationStore.class);
    private final NotificationService service = new NotificationService(repository);

    @Test
    void savesPaymentEventForCorrectUser() {
        service.onPaymentCompleted("payment-1:order-1:user-1");
        verify(repository).save(anyString(), argThat(event ->
                event.paymentId().equals("payment-1") && event.orderId().equals("order-1")
                        && event.userId().equals("user-1")));
    }

    @Test
    void duplicateEventIsSafeToRedeliver() {
        doThrow(new DuplicateKeyException("already received")).when(repository)
                .save(anyString(), any(PaymentCompletedEvent.class));
        assertDoesNotThrow(() -> service.onPaymentCompleted("payment-1:order-1:user-1"));
    }

    @Test
    void malformedEventIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.onPaymentCompleted("not-an-event"));
        verifyNoInteractions(repository);
    }

    @Test
    void listsOnlyRequestedUsersNotifications() {
        when(repository.listForUser("u1")).thenReturn(List.of(Map.of("USER_ID", "u1")));
        assertEquals("u1", service.list("u1").getFirst().get("USER_ID"));
        verify(repository).listForUser("u1");
    }
}
