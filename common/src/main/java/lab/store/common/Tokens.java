package lab.store.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

public final class Tokens {
    private Tokens() { }
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    public static String issue(String userId, String role, String secret, ObjectMapper mapper) {
        try {
            String head = ENC.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String body = ENC.encodeToString(mapper.writeValueAsBytes(Map.of("sub", userId, "role", role, "exp", Instant.now().plusSeconds(3600).getEpochSecond())));
            String data = head + "." + body;
            return data + "." + ENC.encodeToString(sign(data, secret));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> verify(String token, String secret, ObjectMapper mapper) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("Malformed token");
        String data = parts[0] + "." + parts[1];
        if (!java.security.MessageDigest.isEqual(sign(data, secret), Base64.getUrlDecoder().decode(parts[2]))) throw new IllegalArgumentException("Bad signature");
        Map<String, Object> header = mapper.readValue(Base64.getUrlDecoder().decode(parts[0]), Map.class);
        if (!"HS256".equals(header.get("alg"))) throw new IllegalArgumentException("Bad algorithm");
        Map<String, Object> claims = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
        if (((Number) claims.get("exp")).longValue() <= Instant.now().getEpochSecond()) throw new IllegalArgumentException("Expired");
        return claims;
    }
    private static byte[] sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}
