package lab.store.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class RestProductClient implements ProductClient {
    private final RestClient rest;
    private final String productUrl;
    private final String internalKey;

    public RestProductClient(RestClient rest, @Value("${services.product-url}") String productUrl,
                             @Value("${app.internal-key}") String internalKey) {
        this.rest = rest;
        this.productUrl = productUrl;
        this.internalKey = internalKey;
    }

    @Override
    public BigDecimal price(String productId) {
        Map<?, ?> product = rest.get().uri(productUrl + "/api/products/" + productId)
                .retrieve().body(Map.class);
        if (product == null || product.get("PRICE") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service returned no price");
        }
        return new BigDecimal(product.get("PRICE").toString());
    }

    @Override
    public void reserve(String productId, int quantity) {
        rest.post().uri(productUrl + "/internal/products/" + productId + "/reserve")
                .header("X-Internal-Key", internalKey).body(Map.of("quantity", quantity))
                .retrieve().toBodilessEntity();
    }

    @Override
    public void release(String productId, int quantity) {
        rest.post().uri(productUrl + "/internal/products/" + productId + "/release")
                .header("X-Internal-Key", internalKey).body(Map.of("quantity", quantity))
                .retrieve().toBodilessEntity();
    }
}
