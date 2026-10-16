package lab.store.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class RestOrderClient implements OrderClient {
    private final RestClient rest;
    private final String orderUrl;
    private final String internalKey;

    public RestOrderClient(RestClient rest, @Value("${services.order-url}") String orderUrl,
                           @Value("${app.internal-key}") String internalKey) {
        this.rest = rest;
        this.orderUrl = orderUrl;
        this.internalKey = internalKey;
    }

    @Override
    public OrderInfo get(String orderId) {
        Map<?, ?> response = rest.get().uri(orderUrl + "/internal/orders/" + orderId)
                .header("X-Internal-Key", internalKey).retrieve().body(Map.class);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Order service returned no data");
        }
        return new OrderInfo((String) response.get("USER_ID"),
                new BigDecimal(response.get("TOTAL").toString()), (String) response.get("STATUS"));
    }

    @Override
    public void markPaid(String orderId) {
        rest.post().uri(orderUrl + "/internal/orders/" + orderId + "/paid")
                .header("X-Internal-Key", internalKey).retrieve().toBodilessEntity();
    }
}
