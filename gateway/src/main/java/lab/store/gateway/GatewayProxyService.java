package lab.store.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GatewayProxyService {
    private final ServiceRegistry registry;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public GatewayProxyService(ServiceRegistry registry) { this.registry = registry; }

    public ResponseEntity<byte[]> forward(HttpServletRequest req) throws java.io.IOException {
        String path = req.getRequestURI();
        String target = registry.resolve(path);
        String uri = target + path + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
        byte[] body = req.getInputStream().readAllBytes();
        var builder = HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(5))
                .method(req.getMethod(), body.length == 0 ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofByteArray(body));
        for (String header : new String[]{"Authorization", "Content-Type", "X-Request-ID"}) {
            if (req.getHeader(header) != null) builder.header(header, req.getHeader(header));
        }
        HttpResponse<byte[]> response;
        try {
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (java.net.http.HttpTimeoutException ex) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Dependent service timed out");
        } catch (java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Dependent service unavailable");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Gateway request interrupted");
        }
        var headers = new HttpHeaders();
        response.headers().firstValue("content-type").ifPresent(value -> headers.set("Content-Type", value));
        return new ResponseEntity<>(response.body(), headers, HttpStatus.valueOf(response.statusCode()));
    }
}
