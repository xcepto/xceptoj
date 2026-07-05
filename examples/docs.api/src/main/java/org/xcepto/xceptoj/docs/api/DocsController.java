package org.xcepto.xceptoj.docs.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class DocsController {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @GetMapping("/api/ping")
    public ResponseEntity<Void> ping() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/shipment/accept")
    public ResponseEntity<Map<String, Object>> acceptShipment(@RequestBody AmountRequest req) {
        return ResponseEntity.ok(Map.of("amount", req.amount));
    }

    @GetMapping("/inventory/stock")
    public ResponseEntity<Map<String, Object>> getStock() {
        return ResponseEntity.ok(Map.of("replenished", true));
    }

    @PostMapping(value = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> loginJson(@RequestBody LoginRequest req) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, req.username);
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @PostMapping(value = "/auth/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> loginForm(HttpServletRequest request, HttpSession session) {
        String username = param(request, "username", "Username");
        session.setAttribute("username", username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring("Bearer ".length()).trim();
        String username = tokens.get(token);
        if (username == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping(value = "/auth/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> register() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<String> dashboard(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) username = "guest";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body><p>" + username + "</p></body></html>");
    }

    @PostMapping(value = "/token/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body><span>" + token + "</span></body></html>");
    }

    @PostMapping("/api/env/create")
    public ResponseEntity<Void> createEnv(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok().build();
    }

    private String param(HttpServletRequest request, String... names) {
        for (String name : names) {
            String val = request.getParameter(name);
            if (val != null) return val;
        }
        return "";
    }

    static class AmountRequest {
        public int amount;
    }

    static class LoginRequest {
        public String username;
    }
}
