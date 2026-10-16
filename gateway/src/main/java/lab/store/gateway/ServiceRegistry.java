package lab.store.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class ServiceRegistry {
    private final Map<String, String> urls;

    public ServiceRegistry(@Value("${services.auth-url}") String auth,
                           @Value("${services.user-url}") String user,
                           @Value("${services.product-url}") String product,
                           @Value("${services.order-url}") String order,
                           @Value("${services.payment-url}") String payment,
                           @Value("${services.notification-url}") String notification) {
        urls = Map.of("auth", auth, "users", user, "products", product,
                "orders", order, "payments", payment, "notifications", notification);
    }

    public String resolve(String path) {
        if (!path.startsWith("/api/")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown API route");
        }
        String segment = path.substring(5).split("/", 2)[0];
        String url = urls.get(segment);
        if (url == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown API route");
        }
        return url;
    }
}
