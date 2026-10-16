package lab.store.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistryTest {
    private final ServiceRegistry registry = new ServiceRegistry("http://auth", "http://users",
            "http://products", "http://orders", "http://payments", "http://notifications");

    @Test
    void routesEachBusinessAreaToItsService() {
        assertEquals("http://auth", registry.resolve("/api/auth/login"));
        assertEquals("http://users", registry.resolve("/api/users/me"));
        assertEquals("http://products", registry.resolve("/api/products"));
        assertEquals("http://orders", registry.resolve("/api/orders/1"));
        assertEquals("http://payments", registry.resolve("/api/payments"));
        assertEquals("http://notifications", registry.resolve("/api/notifications"));
    }

    @Test
    void rejectsUnknownRoutes() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> registry.resolve("/api/unknown"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
