package lab.store.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestFilter.class);
    private final String jwtSecret;
    private final String internalKey;
    private final ObjectMapper mapper;

    public RequestFilter(@Value("${app.jwt-secret}") String jwtSecret,
                         @Value("${app.internal-key}") String internalKey,
                         ObjectMapper mapper) {
        this.jwtSecret = jwtSecret;
        this.internalKey = internalKey;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String requestId = req.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank() || requestId.length() > 100) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("requestId", requestId);
        res.setHeader("X-Request-ID", requestId);
        long start = System.currentTimeMillis();
        try {
            String path = req.getRequestURI();
            if (path.startsWith("/internal/")) {
                if (!internalKey.equals(req.getHeader("X-Internal-Key"))) {
                    deny(res, 403, "Invalid internal key");
                    return;
                }
            } else if (!isPublic(req.getMethod(), path)) {
                try {
                    String bearer = req.getHeader("Authorization");
                    if (bearer == null || !bearer.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Missing bearer token");
                    }
                    Map<String, Object> claims = Tokens.verify(bearer.substring(7), jwtSecret, mapper);
                    req.setAttribute("userId", claims.get("sub"));
                    req.setAttribute("role", claims.get("role"));
                } catch (Exception ex) {
                    deny(res, 401, "Invalid or expired token");
                    return;
                }
            }
            chain.doFilter(req, res);
        } finally {
            log.info("{} {} status={} durationMs={}", req.getMethod(), req.getRequestURI(),
                    res.getStatus(), System.currentTimeMillis() - start);
            MDC.remove("requestId");
        }
    }

    private boolean isPublic(String method, String path) {
        boolean frontendAsset = method.equals("GET") &&
                (path.equals("/") || path.equals("/index.html") || path.equals("/styles.css")
                        || path.equals("/app.js") || path.equals("/favicon.svg") || path.startsWith("/js/"));
        return path.equals("/health") || path.equals("/ready") || path.startsWith("/actuator/")
                || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
                || path.equals("/api/auth/register") || path.equals("/api/auth/login")
                || (path.startsWith("/api/products") && method.equals("GET")) || frontendAsset;
    }

    private void deny(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        mapper.writeValue(res.getWriter(),
                Map.of("status", status, "error", message, "timestamp", Instant.now().toString()));
    }
}
