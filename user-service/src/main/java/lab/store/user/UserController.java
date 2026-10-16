package lab.store.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @PostMapping("/internal/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@Valid @RequestBody NewUserRequest request) {
        return service.create(request);
    }

    @GetMapping("/api/users/me")
    public Map<String, Object> me(HttpServletRequest request) {
        return service.ownProfile((String) request.getAttribute("userId"));
    }

    @GetMapping("/api/users/{id}")
    public Map<String, Object> get(@PathVariable String id, HttpServletRequest request) {
        return service.profile(id, (String) request.getAttribute("userId"),
                (String) request.getAttribute("role"));
    }
}
