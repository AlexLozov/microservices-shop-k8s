package lab.store.product;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {
    private final ProductStore repository = mock(ProductStore.class);
    private final ProductService service = new ProductService(repository);

    @Test
    void onlyAdminCanCreateProduct() {
        var input = new ProductInput("Keyboard", new BigDecimal("49.90"), 5);
        var ex = assertThrows(ResponseStatusException.class, () -> service.create(input, "USER"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(repository);
        when(repository.find(anyString())).thenAnswer(call -> Optional.of(Map.of("ID", call.getArgument(0))));
        String id = (String) service.create(input, "ADMIN").get("ID");
        verify(repository).save(id, "Keyboard", new BigDecimal("49.90"), 5);
    }

    @Test
    void reserveReportsInsufficientStockWithoutChangingIt() {
        when(repository.reserve("p1", 10)).thenReturn(0);
        var ex = assertThrows(ResponseStatusException.class, () -> service.reserve("p1", 10));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repository, never()).release(anyString(), anyInt());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.release("p1", 0));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(repository);
    }
}
