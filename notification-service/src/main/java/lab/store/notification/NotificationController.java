package lab.store.notification;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping("/api/notifications")
    public List<Map<String, Object>> list(HttpServletRequest request) {
        return service.list((String) request.getAttribute("userId"));
    }
}
