package lab.store.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GatewayController {
    private final GatewayProxyService proxy;

    public GatewayController(GatewayProxyService proxy) { this.proxy = proxy; }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> forward(HttpServletRequest request) throws IOException {
        return proxy.forward(request);
    }
}
